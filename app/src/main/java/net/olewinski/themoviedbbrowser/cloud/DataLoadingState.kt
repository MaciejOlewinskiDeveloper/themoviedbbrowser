package net.olewinski.themoviedbbrowser.cloud

enum class RequestStatus {
    LOADING,
    SUCCESS,
    FAILURE
}

data class DataLoadingState constructor(val requestStatus: RequestStatus, val errorMessage: String? = null) {
    companion object {
        val LOADING = DataLoadingState(RequestStatus.LOADING)
        val SUCCESS = DataLoadingState(RequestStatus.SUCCESS)

        fun error(errorMessage: String?) = DataLoadingState(RequestStatus.FAILURE, errorMessage)
    }
}
