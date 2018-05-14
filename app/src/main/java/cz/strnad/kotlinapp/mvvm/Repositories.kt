package cz.strnad.kotlinapp.mvvm

import android.content.SharedPreferences
import cz.strnad.kotlinapp.api.ApiService
import cz.strnad.kotlinapp.api.Error
import cz.strnad.kotlinapp.api.Translator
import cz.strnad.kotlinapp.database.KotlinAppDatabase
import cz.strnad.kotlinapp.mvvm.ApiViewModel.Companion.errorConverter
import cz.strnad.mvvm.Repository
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

open class ApiRepository<T>(protected val api: ApiService,
                            protected val errorConverter: Converter<ResponseBody, Error>) : Repository<T, Error>() {

    var translator: Translator<Error>? = null

    protected fun processError(e: Throwable) {
        if (e is HttpException) {
            val responseBody = e.response().errorBody()
            val parsedError = errorConverter.convert(responseBody)
            parsedError.translator = translator
            error.value = parsedError
        } else if (e is SocketTimeoutException) {
            error.value = Error.TIMEOUT
        } else if (e is IOException) {
            error.value = Error.DISCONNECTED
        } else {
            error.value = Error.UNKNOWN
        }
    }
}

open class OfflineApiRepository<T>(api: ApiService,
                                   errorConverter: Converter<ResponseBody, Error>,
                                   protected val database: KotlinAppDatabase) : ApiRepository<T>(api, errorConverter) {

}

open class DatabaseRepository<T>(protected val database: KotlinAppDatabase) : Repository<T, Error>() {

}

open class UniversalRepository<T>(protected val api: ApiService,
                                  protected val errorConverter: Converter<ResponseBody, Error>,
                                  protected val database: KotlinAppDatabase,
                                  protected val preferences: SharedPreferences) : Repository<T, Error>() {

}