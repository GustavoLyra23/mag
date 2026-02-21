package core.io

import com.google.gson.Gson
import core.gateways.GithubGateway
import helpers.solvePath
import kotlinx.coroutines.*
import java.nio.file.Path
import kotlin.io.path.readText

class ConfigLoader {
    suspend fun load() = coroutineScope {
        val libraries = solvePath("test_files/load.json")
        val jsonContent = libraries.readText()
        val configContent = Gson()
            .fromJson(jsonContent, LoadConfig::class.java)
        val animJob = launch { AnimateCLI.runLoadAnimation() }
        val jobs: List<Job> = configContent.libraries.map { url ->
            launch(Dispatchers.IO) {
                GithubGateway.instance.getLibrary(
                    url, Path.of(
                        "bibliotecas"
                    )
                )
            }
        }
        jobs.joinAll()
        animJob.cancel()
        println()
    }
}
