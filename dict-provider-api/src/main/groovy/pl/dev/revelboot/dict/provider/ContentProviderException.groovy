package pl.dev.revelboot.dict.provider

class ContentProviderException extends Exception {

    ContentProviderException(String message) {
        super(message)
    }

    ContentProviderException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
