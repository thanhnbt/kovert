package uy.kohesive.kovert.vertx

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import uy.kohesive.kovert.core.HttpVerb
import uy.kohesive.kovert.core.PrefixAsVerbWithSuccessStatus
import uy.kohesive.kovert.core.knownSimpleTypes
import uy.kohesive.kovert.vertx.internal.bindControllerController
import java.time.Instant
import java.time.*
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.temporal.Temporal

private object KovertConfigUpdateJdk8 {
    init {
        // add our JDK 8 simple types to core
        knownSimpleTypes.addAll(listOf(Temporal::class.java,
                OffsetDateTime::class.java,
                ZonedDateTime::class.java,
                LocalDate::class.java,
                LocalDateTime::class.java,
                Clock::class.java,
                Instant::class.java,
                Period::class.java,
                Year::class.java,
                YearMonth::class.java,
                MonthDay::class.java,
                ZoneId::class.java,
                ZoneOffset::class.java,
                LocalTime::class.java,
                OffsetTime::class.java))
    }

    @Suppress("NOTHING_TO_INLINE")
    public inline fun ensure() {
        // TODO: here to be sure we have intiailized anything related before using,
        //       although this function may remain empty it causes initializers on the
        //       object to run.
    }
}

public fun Router.bindController(kotlinClassAsController: Any, atPath: String, verbAliases: List<PrefixAsVerbWithSuccessStatus> = emptyList()) {
    KovertConfigUpdateJdk8.ensure()
    bindControllerController(this, kotlinClassAsController, atPath, verbAliases)
}