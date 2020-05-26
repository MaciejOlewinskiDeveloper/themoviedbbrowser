package net.olewinski.themoviedbbrowser.cloud

enum class RequestStatus {
    PROCEEDING,
    SUCCESS,
    FAILURE
}

data class NetworkDataLoadingState private constructor(val requestStatus: RequestStatus, val errorMessage: String? = null) {
    companion object {
        val LOADING = NetworkDataLoadingState(RequestStatus.PROCEEDING)
        val READY = NetworkDataLoadingState(RequestStatus.SUCCESS)

        fun error(errorMessage: String?) = NetworkDataLoadingState(RequestStatus.FAILURE, errorMessage)
    }
}
