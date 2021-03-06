package uy.kohesive.kovert.vertx.sample

import com.typesafe.config.Config
import io.vertx.ext.web.Router
import nl.komponents.kovenant.functional.bind
import org.slf4j.Logger
import uy.klutter.config.typesafe.KonfigAndInjektMain
import uy.klutter.config.typesafe.KonfigRegistrar
import uy.klutter.config.typesafe.ReferenceConfig
import uy.klutter.config.typesafe.jdk7.FileConfig
import uy.klutter.config.typesafe.loadConfig
import uy.klutter.vertx.VertxWithSlf4jInjektables
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.*
import uy.kohesive.kovert.core.HttpVerb
import uy.kohesive.kovert.core.KovertConfig
import uy.kohesive.kovert.vertx.bindController
import uy.kohesive.kovert.vertx.boot.KovertVerticle
import uy.kohesive.kovert.vertx.boot.KovertVerticleModule
import uy.kohesive.kovert.vertx.boot.KovertVertx
import uy.kohesive.kovert.vertx.boot.KovertVertxModule
import java.nio.file.Path
import java.nio.file.Paths

public class App(val configFile: Path) {
    companion object {
        @JvmStatic public fun main(args: Array<String>) {
            if (args.size != 1) {
                println("Invalid usage.  ConfigFile parameter is required!")
                println()
                println("  usage:  App <configFile>")
                println()
                println("There is a sample config file in web/sample.conf under the root of this sample project")
                println()
                System.exit(-1)
            }
            App(Paths.get(args[0])).start()
        }
    }

    // injection setup is done in a nested object to control the order of instantiation AFTER the configFile member is available
    val injektMain = object : KonfigAndInjektMain() {
        override fun configFactory(): Config {
            return loadConfig(FileConfig(configFile), ReferenceConfig())
        }

        override fun KonfigRegistrar.registerConfigurables() {
            // configuration for launching vertx, we could also pass configuration in directly to the constructor of KovertVertx
            importModule("kovert.vertx", KovertVertxModule)
            // configuration for kovert as a verticle, we could also pass configuration in directly to the constructor of KovertVerticle
            importModule("kovert.server", KovertVerticleModule)
        }

        override fun InjektRegistrar.registerInjectables() {
            // includes jackson ObjectMapper to match compatibility with Vertx, app logging via Vertx facade to Slf4j
            importModule(VertxWithSlf4jInjektables)
            // everything Kovert wants
            importModule(KovertVertxModule)
            importModule(KovertVerticleModule)
            // our controllers like to use services
            importModule(MockAuthService.Injektables)
            importModule(MockPeopleService.Injektables)
            importModule(MockCompanyService.Injektables)
        }
    }

    public fun start() {
        // we use the pattern findSomething so add that as a alias for HTTP GET
        KovertConfig.addVerbAlias("find", HttpVerb.GET)
        val LOG: Logger = Injekt.logger(this)
        val apiMountPoint = "api"

        val initControllers = fun Router.(): Unit {
            // bind the controller classes
            bindController(PeopleController(), apiMountPoint)
            bindController(CompanyController(), apiMountPoint)
        }
        val configFileLocation = configFile.getParent() // make things relative to the config file location

        // startup synchronously...
        KovertVertx.start(workingDir = configFileLocation) bind { vertx ->
            KovertVerticle.deploy(vertx, routerInit = initControllers)
        } success { deploymentId ->
            LOG.warn("Deployment complete.")
        } fail { error ->
            LOG.error("Deployment failed!", error)
        }
    }
}
