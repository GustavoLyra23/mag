package core.io

import com.google.gson.annotations.SerializedName

data class LoadConfig(
    @SerializedName("versao")
    val version: String,
    @SerializedName("bibliotecas")
    val libraries: List<String>
)
