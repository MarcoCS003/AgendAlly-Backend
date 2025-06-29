package com.academically

import com.example.ClientType
import com.example.routes.eventsRoutes
import database.initDatabase
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import routes.*

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val host = "0.0.0.0"

    println("🚀 Academic Ally Backend - PRODUCTION READY")
    println("📡 Host: $host")
    println("🔌 Port: $port")
    println("🔥 Auth: Firebase Direct")
    println("🏢 Organizations: Auto-assignment by email")

    embeddedServer(Netty, port = port, host = host, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureFirebase()
    configureDatabase()
    configureSerialization()
    configureCORS()
    configureHeaders()
    configureRouting()
}

fun Application.configureFirebase() {
    try {
        println("🔥 Inicializando Firebase...")
        System.setProperty("ENVIRONMENT", "development")
        FirebaseConfig.initialize()
        println("✅ Firebase listo")
    } catch (e: Exception) {
        println("⚠️ Firebase error: ${e.message} - Continuando...")
    }
}

fun Application.configureDatabase() {
    println("📊 Inicializando BD...")
    initDatabase()
    println("✅ BD lista")
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
        allowHeader("X-Client-Type")
        anyHost() // Para desarrollo
    }
}

fun Application.configureHeaders() {
    install(DefaultHeaders) {
        header("X-Engine", "Academic Ally")
        header("X-Version", "2.0.0")
    }
}

fun Application.configureRouting() {
    routing {
        // ===== RUTAS PÚBLICAS =====
        get("/") {
            call.respondText("""
                {
                    "message": "🎓 Academic Ally Backend API",
                    "version": "2.0.0",
                    "status": "running",
                    "endpoints": {
                        "public": [
                            "GET /health",
                            "GET /api/organizations", 
                            "GET /api/channels",
                            "GET /api/events",
                            "GET /api/auth/client-info",
                            "GET /images/{filename}",
                            "GET /static/{path}",
                            "GET /api/images/{filename}"
                        ],
                        "protected": [
                            "GET /api/auth/me (requires: Authorization + X-Client-Type)"
                        ]
                    },
                    "auth_flow": {
                        "step_1": "App autentica con Firebase Auth",
                        "step_2": "App obtiene idToken de Firebase",
                        "step_3": "App envía: Authorization: Bearer <idToken>, X-Client-Type: ANDROID_STUDENT|DESKTOP_ADMIN",
                        "step_4": "Backend valida y asigna permisos automáticamente"
                    },
                    "static_files": {
                        "images": "/images/{filename}",
                        "static": "/static/{path}",
                        "api_images": "/api/images/{filename}"
                    }
                }
            """.trimIndent(), ContentType.Application.Json)
        }

        get("/health") {
            call.respondText("""
                {
                    "status": "healthy",
                    "timestamp": ${System.currentTimeMillis()},
                    "firebase": "${if (FirebaseConfig.isReady()) "ready" else "not_configured"}",
                    "database": "connected",
                    "version": "2.0.0",
                    "static_files": "enabled"
                }
            """.trimIndent(), ContentType.Application.Json)
        }

        // ===== RUTAS MODULARES =====
        organizationRoutes()    // /api/organizations/*
        channelsRoutes()        // /api/channels/*
        eventsRoutes()          // /api/events/*
        authRoutes()            // /api/auth/*
        staticFilesRoutes()     // /images/*, /static/*, /api/images/*
    }
}