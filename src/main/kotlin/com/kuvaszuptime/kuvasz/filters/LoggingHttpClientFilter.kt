package com.kuvaszuptime.kuvasz.filters

import com.kuvaszuptime.kuvasz.services.HttpCommunicationLogger
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import javax.inject.Inject

@Filter("/**")
@Requires(property = "http-communication-log.enabled", value = "true")
class LoggingHttpClientFilter
@Inject constructor(private val service: HttpCommunicationLogger) : HttpClientFilter {

    override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> =
        Flowable
            .fromCallable { service.log(request) }
            .switchMap { chain.proceed(request) }
            .doOnNext { response -> service.log(request, response) }
            .doOnError { error ->
                if (error is HttpClientResponseException) {
                    service.log(request, error.response)
                }
            }
}
