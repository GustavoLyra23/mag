import core.Interpreter
import core.gateways.GithubGateway
import core.io.AnimateCLI
import core.io.ConfigLoader
import helpers.solvePath
import helpers.validateFile
import kotlinx.coroutines.*
import org.antlr.v4.runtime.*
import org.gustavolyra.MagLexer
import org.gustavolyra.MagParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.system.exitProcess

var interpreter = Interpreter()
val STATIC_PATH: Path = Path.of(System.getProperty("user.dir")) //cwd
val mainLog: Logger = LoggerFactory.getLogger("Main")

suspend fun main() {
    mainLog.info("Iniciando Interpretador Mag")
    interactiveMode();
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun interactiveMode() {

    mainLog.info("Digite 'sair' para interromper o programa")
    mainLog.info("Digite 'run <caminho>' para executar um arquivo")
    while (true) {
        print("→ ")
        val input = readlnOrNull()?.trim() ?: continue
        when {
            input == "sair" -> exitProcess(0)
            input.startsWith("importar") -> {
                val inputArray = input.split(" ");
                runBlocking {
                    val animJob: Job = launch {
                        AnimateCLI.runLoadAnimation()
                    }
                    val downloadJob: Job = launch(Dispatchers.IO) {
                        GithubGateway.instance.getLibrary(
                            inputArray[1], Path.of("bibliotecas")
                        )
                    }
                    downloadJob.join()
                    animJob.cancel()
                }
                println()
            }

            input.startsWith("run ") -> {
                val path = input.substring(4).trim()
                val filePath = solvePath(path)
                execFile(filePath)
            }

            input == "reset" -> interpreter = Interpreter()
            else -> execInterpreter(input)
        }
    }
}

suspend fun execFile(filePath: Path) {
    try {
        ConfigLoader().load()
        if (!validateFile(filePath.toFile())) return
        val fileData = filePath.readText()
        execInterpreter(fileData)
    } catch (e: Exception) {
        mainLog.error("Erro ao ler/executar o arquivo: ${e.message}")
    }
}


fun execInterpreter(code: String) {
    try {
        val input = CharStreams.fromString(code)
        val lexer = MagLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = MagParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?
            ) {
                mainLog.error("Erro de sintaxe na linha $line:$charPositionInLine")
            }
        })

        val tree = parser.programa()
        if (tree == null) {
            mainLog.error("Analise sintatica falhou arvore sintatica nula!")
            return
        }
        interpreter.interpret(tree)
    } catch (e: Exception) {
        mainLog.error("Erro ao executar o programa ${e.message}")
    }
}


