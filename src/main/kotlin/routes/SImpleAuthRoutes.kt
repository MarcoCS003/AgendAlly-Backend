package routes

import com.example.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import services.AuthMiddleware

fun Route.authRoutes() {
    val authMiddleware = AuthMiddleware()

    route("/api/auth") {

        // GET /api/auth/client-info - Información sobre tipos de cliente
        get("/client-info") {
            call.respond(HttpStatusCode.OK, mapOf(
                "client_types" to mapOf(
                    "ANDROID_STUDENT" to mapOf(
                        "role" to "STUDENT",
                        "description" to "App móvil para estudiantes",
                        "permissions" to listOf("Ver eventos", "Suscribirse a canales")
                    ),
                    "DESKTOP_ADMIN" to mapOf(
                        "role" to "ADMIN",
                        "description" to "App desktop para administradores",
                        "permissions" to listOf("Crear eventos", "Gestionar canales", "Ver reportes")
                    ),
                    "WEB_ADMIN" to mapOf(
                        "role" to "ADMIN",
                        "description" to "Dashboard web para administradores",
                        "permissions" to listOf("Gestión completa", "Reportes avanzados")
                    )
                ),
                "headers_required" to mapOf(
                    "Authorization" to "Bearer <firebase_id_token>",
                    "X-Client-Type" to "ANDROID_STUDENT | DESKTOP_ADMIN | WEB_ADMIN"
                ),
                "flow" to listOf(
                    "1. App autentica con Firebase Auth directamente",
                    "2. App obtiene idToken de Firebase",
                    "3. App envía idToken + X-Client-Type al backend",
                    "4. Backend valida token y asigna permisos según plataforma"
                )
            ))
        }

        // GET /api/auth/me - Obtener perfil del usuario autenticado
        get("/me") {
            try {
                val authHeader = call.request.headers["Authorization"]
                val clientTypeHeader = call.request.headers["X-Client-Type"]

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(error = "Token requerido: Authorization: Bearer <firebase_token>")
                    )
                    return@get
                }

                val clientType = try {
                    ClientType.valueOf(clientTypeHeader ?: "UNKNOWN")
                } catch (e: Exception) {
                    ClientType.UNKNOWN
                }

                val idToken = authHeader.substring(7)

                // Validar token con Firebase y obtener usuario
                val authResult = authMiddleware.validateTokenAndPermissions(idToken, clientType)

                if (authResult != null) {
                    call.respond(HttpStatusCode.OK, authResult.user)
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(error = "Token inválido o expirado")
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error validando usuario: ${e.message}")
                )
            }
        }

        // POST /api/auth/google - Autenticación con Google/Firebase
        post("/google") {
            try {
                // TODO: Implementar cuando se active Firebase Auth
                call.respond(
                    HttpStatusCode.NotImplemented,
                    ErrorResponse(error = "Autenticación Firebase pendiente de implementación")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error en autenticación: ${e.message}")
                )
            }
        }

        // POST /api/auth/validate - Validar token existente
        post("/validate") {
            try {
                // TODO: Implementar validación de token existente
                call.respond(
                    HttpStatusCode.NotImplemented,
                    ErrorResponse(error = "Validación de token pendiente de implementación")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error validando token: ${e.message}")
                )
            }
        }

        // GET /api/auth/firebase-status - Estado de Firebase
        get("/firebase-status") {
            try {
                val firebaseReady = FirebaseConfig.isReady()
                call.respond(HttpStatusCode.OK, mapOf(
                    "firebase_initialized" to firebaseReady,
                    "project_id" to if (firebaseReady) {
                        com.google.firebase.FirebaseApp.getInstance().options.projectId
                    } else {
                        "No disponible"
                    },
                    "environment" to "development",
                    "auth_flow" to "Simplified - Apps → Firebase → Backend"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Firebase no inicializado: ${e.message}",
                    "setup_help" to "Verifica firebase-service-account.json"
                ))
            }
        }
    }

    // 🛠️ RUTAS DE DESARROLLO (solo en development)
    if (System.getenv("ENVIRONMENT") == "development") {
        route("/api/dev/auth") {

            get("/help") {
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Ayuda para desarrollo de autenticación",
                    "steps" to listOf(
                        "1. Configurar Firebase Auth en tu app",
                        "2. Hacer login con Firebase Auth",
                        "3. Obtener idToken: user.getIdToken()",
                        "4. Enviar al backend con headers correctos"
                    ),
                    "test_endpoints" to mapOf(
                        "validate" to "POST /api/auth/validate",
                        "profile" to "GET /api/auth/me"
                    ),
                    "example_headers" to mapOf(
                        "Authorization" to "Bearer <firebase_id_token>",
                        "X-Client-Type" to "ANDROID_STUDENT"
                    )
                ))
            }

            // GET /api/dev/auth/test - Endpoint de prueba sin autenticación
            get("/test") {
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Endpoint de prueba - sin autenticación requerida",
                    "timestamp" to System.currentTimeMillis(),
                    "client_ip" to call.request.origin.remoteHost,
                    "user_agent" to call.request.headers["User-Agent"]
                ))
            }
        }
    }
}