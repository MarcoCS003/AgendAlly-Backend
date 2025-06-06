package com.academically

import com.example.database.initDatabase
import com.example.routes.blogEventsRoutes
import com.example.routes.instituteRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureCORS()
    configureHeaders()
    configureRouting()
}

fun Application.configureDatabase() {
    initDatabase()
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowHost("localhost:8081")
        allowHost("10.0.2.2:8081")
        allowHost("192.168.100.2:8081")

        anyHost()
    }
}

fun Application.configureHeaders() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
}

fun Application.configureRouting() {
    routing {
        // Ruta principal - Health check
        get("/") {
            call.respondText(
                """
                {
                    "message": "🎓 Academic Ally Backend API",
                    "version": "1.0.0",
                    "status": "running",
                    "endpoints": [
                        "GET /api/institutes - Obtener todos los institutos",
                        "GET /api/institutes/search?q=query - Buscar institutos",
                        "GET /api/institutes/{id} - Obtener instituto específico",
                        "GET /api/institutes/stats - Estadísticas de institutos",
                        "GET /api/events - Obtener todos los eventos del blog",
                        "GET /api/events/search?q=query - Buscar eventos",
                        "GET /api/events/category/{category} - Eventos por categoría",
                        "GET /api/events/upcoming - Eventos próximos",
                        "GET /api/events/{id} - Obtener evento específico",
                        "GET /api/institutes/{id}/events - Eventos de un instituto",
                        "POST /api/events - Crear nuevo evento",
                        "PUT /api/events/{id} - Actualizar evento",
                        "DELETE /api/events/{id} - Eliminar evento"
                    ]
                }
                """.trimIndent(),
                ContentType.Application.Json
            )
        }

        // Health check endpoint
        get("/health") {
            call.respondText(
                """
                {
                    "status": "healthy",
                    "timestamp": ${System.currentTimeMillis()},
                    "database": "connected",
                    "services": ["institutes", "blog-events"]
                }
                """.trimIndent(),
                ContentType.Application.Json
            )
        }

        // Configurar rutas
        instituteRoutes()
        blogEventsRoutes() // Nueva ruta para eventos del blog
    }
}