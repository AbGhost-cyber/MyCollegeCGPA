package com.crushtech.mycollegecgpa.utils

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }

    /**
     * @param query
     *     returns cached entries from the local room database
     * @param fetch
     *       Gets up-to-date entries from the api
     * @param saveFetchResult
     *         caches up-to-date entries
     * @param onFetchFailed
     *    handles errors occurring while getting up-to date data
     *   @param shouldFetch
     *        determines if we load data from the api or from the local cache
     *  @author "Dremo.dev"
     */

) = flow {
    emit(Resource.loading(null))

    // get the first element emitted
    val data = query().first()

    //check if need be to fetch
    val flow = if (shouldFetch(data)) {
        emit(Resource.loading(data))

        try {
            //get data from ktor server
            val fetchedResult = fetch()

            //insert data into database
            saveFetchResult(fetchedResult)
            query().map {
                Resource.success(it)
            }
        } catch (t: Throwable) {
            onFetchFailed(t)
            query().map {
                Resource.error(
                    "Couldn't reach server,please try again",
                    it
                )
            }
        }
    } else {
        query().map {
            Resource.success(it)
        }
    }
    emitAll(flow)
}