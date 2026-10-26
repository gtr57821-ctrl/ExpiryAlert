package com.example.expiryalert

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expiryalert.ui.theme.ExpiryAlertTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

  private val solicitarPermisoNotificaciones =
    registerForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { concedido ->

      if (concedido) {
        Toast.makeText(
          this,
          "Notificaciones activadas",
          Toast.LENGTH_SHORT
        ).show()

        NotificationHelper.mostrarNotificacionPrueba(this)
      } else {
        Toast.makeText(
          this,
          "Permiso de notificaciones no concedido",
          Toast.LENGTH_SHORT
        ).show()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()

    NotificationHelper.crearCanalNotificaciones(this)
    pedirPermisoNotificaciones()

    setContent {
      ExpiryAlertTheme {
        ExpiryAlertApp()
      }
    }
  }

  private fun pedirPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
      ) {
        solicitarPermisoNotificaciones.launch(
          Manifest.permission.POST_NOTIFICATIONS
        )
      } else {
        NotificationHelper.mostrarNotificacionPrueba(this)
      }
    } else {
      NotificationHelper.mostrarNotificacionPrueba(this)
    }
  }
}

@Composable
fun ExpiryAlertApp() {

  var pantalla by remember {
    mutableStateOf(
      if (FirebaseAuth.getInstance().currentUser != null) {
        "principal"
      } else {
        "inicio"
      }
    )
  }

  var productoEditar by remember {
    mutableStateOf<Map<String, Any>?>(null)
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color(0xFFF7F5FA)
  ) {

    when (pantalla) {

      "inicio" -> PantallaInicio(
        irLogin = {
          pantalla = "login"
        },
        irRegistro = {
          pantalla = "registro"
        }
      )

      "login" -> PantallaLogin(
        regresar = {
          pantalla = "inicio"
        },
        irRegistro = {
          pantalla = "registro"
        },
        loginExitoso = {
          pantalla = "principal"
        }
      )

      "registro" -> PantallaRegistro(
        regresar = {
          pantalla = "inicio"
        },
        irLogin = {
          pantalla = "login"
        },
        registroExitoso = {
          pantalla = "principal"
        }
      )

      "principal" -> PantallaPrincipal(
        agregarProducto = {
          pantalla = "agregarProducto"
        },
        editarProducto = { producto ->
          productoEditar = producto
          pantalla = "editarProducto"
        },
        cerrarSesion = {
          FirebaseAuth.getInstance().signOut()
          pantalla = "inicio"
        }
      )

      "agregarProducto" -> PantallaAgregarProducto(
        regresar = {
          pantalla = "principal"
        },
        productoGuardado = {
          pantalla = "principal"
        }
      )

      "editarProducto" -> {

        val producto = productoEditar

        if (producto != null) {

          PantallaEditarProducto(
            producto = producto,
            regresar = {
              productoEditar = null
              pantalla = "principal"
            },
            productoActualizado = {
              productoEditar = null
              pantalla = "principal"
            }
          )

        } else {
          pantalla = "principal"
        }
      }
    }
  }
}

// ----------------------------------------------------
// PANTALLA DE INICIO
// ----------------------------------------------------

@Composable
fun PantallaInicio(
  irLogin: () -> Unit,
  irRegistro: () -> Unit
) {

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF7F5FA))
      .padding(horizontal = 24.dp)
      .padding(top = 90.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      text = "ExpiryAlert",
      fontSize = 36.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF6750A4)
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "Controla las fechas de caducidad\nde tus productos",
      fontSize = 18.sp,
      color = Color.DarkGray,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(50.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Color.White,
          RoundedCornerShape(20.dp)
        )
        .padding(24.dp)
    ) {

      Column {

        Text(
          text = "Bienvenido",
          fontSize = 25.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Registra tus productos y recibe alertas antes de que caduquen.",
          fontSize = 16.sp,
          color = Color.Gray
        )
      }
    }

    Spacer(modifier = Modifier.height(40.dp))

    Button(
      onClick = irLogin,
      modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
      shape = RoundedCornerShape(15.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6750A4),
        contentColor = Color.White
      )
    ) {

      Text(
        text = "Iniciar sesión",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(15.dp))

    Button(
      onClick = irRegistro,
      modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
      shape = RoundedCornerShape(15.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF9A82DB),
        contentColor = Color.White
      )
    ) {

      Text(
        text = "Crear cuenta",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(35.dp))

    Text(
      text = "Nunca olvides una fecha de caducidad",
      fontSize = 14.sp,
      color = Color.Gray
    )
  }
}

// ----------------------------------------------------
// INICIAR SESIÓN
// ----------------------------------------------------

@Composable
fun PantallaLogin(
  regresar: () -> Unit,
  irRegistro: () -> Unit,
  loginExitoso: () -> Unit
) {

  var correo by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var cargando by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val auth = FirebaseAuth.getInstance()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF7F5FA))
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp)
      .padding(top = 70.dp, bottom = 30.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      text = "ExpiryAlert",
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF6750A4)
    )

    Spacer(modifier = Modifier.height(35.dp))

    Text(
      text = "Iniciar sesión",
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Ingresa a tu cuenta para continuar",
      fontSize = 16.sp,
      color = Color.Gray
    )

    Spacer(modifier = Modifier.height(35.dp))

    OutlinedTextField(
      value = correo,
      onValueChange = { correo = it },
      label = {
        Text("Correo electrónico")
      },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email
      )
    )

    Spacer(modifier = Modifier.height(15.dp))

    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = {
        Text("Contraseña")
      },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password
      )
    )

    Spacer(modifier = Modifier.height(30.dp))

    Button(
      onClick = {

        if (correo.isBlank() || password.isBlank()) {

          Toast.makeText(
            context,
            "Completa todos los campos",
            Toast.LENGTH_SHORT
          ).show()

        } else {

          cargando = true

          auth.signInWithEmailAndPassword(
            correo.trim(),
            password
          ).addOnCompleteListener { task ->

            cargando = false

            if (task.isSuccessful) {

              Toast.makeText(
                context,
                "Inicio de sesión correcto",
                Toast.LENGTH_SHORT
              ).show()

              loginExitoso()

            } else {

              Toast.makeText(
                context,
                "No se pudo iniciar sesión: ${task.exception?.localizedMessage}",
                Toast.LENGTH_LONG
              ).show()
            }
          }
        }
      },
      enabled = !cargando,
      modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6750A4),
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(15.dp)
    ) {

      if (cargando) {

        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = Color.White,
          strokeWidth = 2.dp
        )

      } else {

        Text(
          text = "Entrar",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(15.dp))

    TextButton(
      onClick = irRegistro
    ) {

      Text(
        text = "¿No tienes cuenta? Crear cuenta",
        color = Color(0xFF6750A4)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    TextButton(
      onClick = regresar
    ) {

      Text(
        text = "← Volver",
        color = Color.Gray
      )
    }
  }
}

// ----------------------------------------------------
// CREAR CUENTA
// ----------------------------------------------------

@Composable
fun PantallaRegistro(
  regresar: () -> Unit,
  irLogin: () -> Unit,
  registroExitoso: () -> Unit
) {

  var nombre by remember { mutableStateOf("") }
  var correo by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmarPassword by remember { mutableStateOf("") }
  var cargando by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val auth = FirebaseAuth.getInstance()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF7F5FA))
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp)
      .padding(top = 55.dp, bottom = 30.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      text = "ExpiryAlert",
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF6750A4)
    )

    Spacer(modifier = Modifier.height(25.dp))

    Text(
      text = "Crear cuenta",
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Crea tu cuenta para comenzar",
      color = Color.Gray,
      fontSize = 16.sp
    )

    Spacer(modifier = Modifier.height(25.dp))

    OutlinedTextField(
      value = nombre,
      onValueChange = { nombre = it },
      label = { Text("Nombre") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = correo,
      onValueChange = { correo = it },
      label = { Text("Correo electrónico") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email
      )
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Contraseña") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password
      )
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = confirmarPassword,
      onValueChange = { confirmarPassword = it },
      label = { Text("Confirmar contraseña") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password
      )
    )

    Spacer(modifier = Modifier.height(25.dp))

    Button(
      onClick = {

        when {

          nombre.isBlank() ||
            correo.isBlank() ||
            password.isBlank() ||
            confirmarPassword.isBlank() -> {

            Toast.makeText(
              context,
              "Completa todos los campos",
              Toast.LENGTH_SHORT
            ).show()
          }

          password != confirmarPassword -> {

            Toast.makeText(
              context,
              "Las contraseñas no coinciden",
              Toast.LENGTH_SHORT
            ).show()
          }

          password.length < 6 -> {

            Toast.makeText(
              context,
              "La contraseña debe tener al menos 6 caracteres",
              Toast.LENGTH_SHORT
            ).show()
          }

          else -> {

            cargando = true

            auth.createUserWithEmailAndPassword(
              correo.trim(),
              password
            ).addOnCompleteListener { task ->

              cargando = false

              if (task.isSuccessful) {

                Toast.makeText(
                  context,
                  "Cuenta creada correctamente",
                  Toast.LENGTH_SHORT
                ).show()

                registroExitoso()

              } else {

                Toast.makeText(
                  context,
                  "Error al crear cuenta: ${task.exception?.localizedMessage}",
                  Toast.LENGTH_LONG
                ).show()
              }
            }
          }
        }
      },
      enabled = !cargando,
      modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6750A4),
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(15.dp)
    ) {

      if (cargando) {

        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = Color.White,
          strokeWidth = 2.dp
        )

      } else {

        Text(
          text = "Registrarme",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    TextButton(
      onClick = irLogin
    ) {
      Text(
        text = "¿Ya tienes cuenta? Iniciar sesión",
        color = Color(0xFF6750A4)
      )
    }

    TextButton(
      onClick = regresar
    ) {
      Text(
        text = "← Volver",
        color = Color.Gray
      )
    }
  }
}

// ----------------------------------------------------
// PANTALLA PRINCIPAL
// ----------------------------------------------------

@Composable
fun PantallaPrincipal(
  agregarProducto: () -> Unit,
  editarProducto: (Map<String, Any>) -> Unit,
  cerrarSesion: () -> Unit
) {

  val usuario = FirebaseAuth.getInstance().currentUser
  val db = FirebaseFirestore.getInstance()
  val context = LocalContext.current

  var productos by remember {
    mutableStateOf<List<Map<String, Any>>>(emptyList())
  }

  var cargando by remember { mutableStateOf(true) }
  var errorCarga by remember { mutableStateOf<String?>(null) }

  var productoEliminar by remember {
    mutableStateOf<Map<String, Any>?>(null)
  }

  DisposableEffect(usuario?.uid) {

    if (usuario == null) {

      cargando = false
      errorCarga = "No hay una sesión iniciada."

      onDispose { }

    } else {

      val listener = db.collection("productos")
        .whereEqualTo("usuarioId", usuario.uid)
        .addSnapshotListener { snapshot, error ->

          if (error != null) {
            cargando = false
            errorCarga = error.localizedMessage
            return@addSnapshotListener
          }

          if (snapshot != null) {

            productos = snapshot.documents.mapNotNull { documento ->

              val datos = documento.data?.toMutableMap()

              if (datos != null) {
                datos["id"] = documento.id
              }

              datos
            }

            cargando = false
            errorCarga = null
          }
        }

      onDispose {
        listener.remove()
      }
    }
  }

  // ------------------------------------------------
  // CONFIRMACIÓN PARA ELIMINAR
  // ------------------------------------------------

  productoEliminar?.let { producto ->

    AlertDialog(
      onDismissRequest = {
        productoEliminar = null
      },

      title = {
        Text(
          text = "Eliminar producto",
          fontWeight = FontWeight.Bold
        )
      },

      text = {
        Text(
          text = "¿Seguro que deseas eliminar \"${producto["nombre"]}\"?\n\nEsta acción no se puede deshacer."
        )
      },

      confirmButton = {

        Button(
          onClick = {

            val id = producto["id"]?.toString()

            if (id.isNullOrBlank()) {

              Toast.makeText(
                context,
                "No se encontró el ID del producto",
                Toast.LENGTH_SHORT
              ).show()

              productoEliminar = null

            } else {

              db.collection("productos")
                .document(id)
                .delete()
                .addOnSuccessListener {

                  Toast.makeText(
                    context,
                    "Producto eliminado",
                    Toast.LENGTH_SHORT
                  ).show()

                  productoEliminar = null
                }
                .addOnFailureListener { error ->

                  Toast.makeText(
                    context,
                    "Error al eliminar: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                  ).show()

                  productoEliminar = null
                }
            }
          },

          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White
          )
        ) {

          Text(
            text = "Eliminar",
            fontWeight = FontWeight.Bold
          )
        }
      },

      dismissButton = {

        TextButton(
          onClick = {
            productoEliminar = null
          }
        ) {

          Text(
            text = "Cancelar",
            color = Color.Gray
          )
        }
      }
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF7F5FA))
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp)
      .padding(top = 80.dp, bottom = 30.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      text = "ExpiryAlert",
      fontSize = 34.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF6750A4)
    )

    Spacer(modifier = Modifier.height(30.dp))

    Text(
      text = "¡Bienvenido!",
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = usuario?.email ?: "",
      fontSize = 16.sp,
      color = Color.Gray
    )

    Spacer(modifier = Modifier.height(35.dp))

    Text(
      text = "Mis productos",
      fontSize = 25.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(15.dp))

    when {

      cargando -> {

        Spacer(modifier = Modifier.height(25.dp))

        CircularProgressIndicator(
          color = Color(0xFF6750A4)
        )

        Spacer(modifier = Modifier.height(25.dp))
      }

      errorCarga != null -> {

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Color.White,
              RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
        ) {

          Column {

            Text(
              text = "No se pudieron cargar los productos",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = errorCarga ?: "",
              color = Color.Gray,
              fontSize = 14.sp
            )
          }
        }
      }

      productos.isEmpty() -> {

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Color.White,
              RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
        ) {

          Column {

            Text(
              text = "Todavía no tienes productos",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Presiona “Agregar producto” para registrar el primero.",
              fontSize = 16.sp,
              color = Color.Gray
            )
          }
        }
      }

      else -> {

        productos.forEach { producto ->

          TarjetaProducto(
            producto = producto,

            editar = {
              editarProducto(producto)
            },

            eliminar = {
              productoEliminar = producto
            }
          )

          Spacer(modifier = Modifier.height(15.dp))
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = agregarProducto,
      modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6750A4),
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(15.dp)
    ) {

      Text(
        text = "+ Agregar producto",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    TextButton(
      onClick = cerrarSesion
    ) {

      Text(
        text = "Cerrar sesión",
        color = Color.Gray
      )
    }
  }
}

// ----------------------------------------------------
// TARJETA DEL PRODUCTO
// ----------------------------------------------------

@Composable
fun TarjetaProducto(
  producto: Map<String, Any>,
  editar: () -> Unit,
  eliminar: () -> Unit
) {

  val nombre =
    producto["nombre"]?.toString() ?: "Sin nombre"

  val categoria =
    producto["categoria"]?.toString() ?: "Sin categoría"

  val cantidad =
    producto["cantidad"]?.toString() ?: "0"

  val fechaCaducidad =
    producto["fechaCaducidad"]?.toString() ?: ""

  val estado =
    calcularEstadoCaducidad(fechaCaducidad)

  val colorEstado = when (estado) {

    "Vencido" ->
      Color(0xFFD32F2F)

    "Próximo a caducar" ->
      Color(0xFFF57C00)

    else ->
      Color(0xFF2E7D32)
  }

  val emojiEstado = when (estado) {

    "Vencido" -> "🔴"

    "Próximo a caducar" -> "🟡"

    else -> "🟢"
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color.White
    )
  ) {

    Column(
      modifier = Modifier.padding(22.dp)
    ) {

      Text(
        text = nombre,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Categoría: $categoria",
        fontSize = 16.sp,
        color = Color.Gray
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Cantidad: $cantidad",
        fontSize = 16.sp,
        color = Color.Gray
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Caduca: $fechaCaducidad",
        fontSize = 16.sp,
        color = Color.Gray
      )

      Spacer(modifier = Modifier.height(15.dp))

      Text(
        text = "$emojiEstado $estado",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = colorEstado
      )

      Spacer(modifier = Modifier.height(20.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {

        OutlinedButton(
          onClick = editar,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        ) {

          Text(
            text = "✏️ Editar",
            color = Color(0xFF6750A4),
            fontWeight = FontWeight.Bold
          )
        }

        OutlinedButton(
          onClick = eliminar,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        ) {

          Text(
            text = "🗑️ Eliminar",
            color = Color(0xFFD32F2F),
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

// ----------------------------------------------------
// CALCULAR ESTADO
// ----------------------------------------------------

fun calcularEstadoCaducidad(
  fechaCaducidad: String
): String {

  return try {

    val formato =
      SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
      )

    formato.isLenient = false

    val fechaProducto =
      formato.parse(fechaCaducidad)
        ?: return "Seguro"

    val hoy =
      Calendar.getInstance().apply {

        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

    val fechaFinal =
      Calendar.getInstance().apply {

        time = fechaProducto

        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

    val diferencia =
      fechaFinal.timeInMillis -
        hoy.timeInMillis

    val diasRestantes =
      TimeUnit.MILLISECONDS.toDays(
        diferencia
      )

    when {

      diasRestantes < 0 ->
        "Vencido"

      diasRestantes <= 7 ->
        "Próximo a caducar"

      else ->
        "Seguro"
    }

  } catch (e: Exception) {

    "Seguro"
  }
}

// ----------------------------------------------------
// AGREGAR PRODUCTO
// ----------------------------------------------------

@Composable
fun PantallaAgregarProducto(
  regresar: () -> Unit,
  productoGuardado: () -> Unit
) {

  var nombre by remember { mutableStateOf("") }
  var categoria by remember { mutableStateOf("") }
  var cantidad by remember { mutableStateOf("") }
  var fechaCaducidad by remember { mutableStateOf("") }
  var cargando by remember { mutableStateOf(false) }

  val context = LocalContext.current

  val auth = FirebaseAuth.getInstance()
  val db = FirebaseFirestore.getInstance()

  var codigoBarras by remember { mutableStateOf("") }
  val (escanearCodigo, escanearFecha) = rememberEscanearProducto(
    alLeerCodigo = { codigo, nombreEncontrado, categoriaEncontrada ->
      codigoBarras = codigo
      if (!nombreEncontrado.isNullOrBlank()) nombre = nombreEncontrado
      if (!categoriaEncontrada.isNullOrBlank()) categoria = categoriaEncontrada
    },
    alLeerFecha = { fechaCaducidad = it }
  )

  val calendario = Calendar.getInstance()

  val selectorFecha = DatePickerDialog(
    context,
    { _, year, month, day ->

      val fechaSeleccionada =
        Calendar.getInstance()

      fechaSeleccionada.set(
        year,
        month,
        day
      )

      val formato =
        SimpleDateFormat(
          "dd/MM/yyyy",
          Locale.getDefault()
        )

      fechaCaducidad =
        formato.format(
          fechaSeleccionada.time
        )
    },
    calendario.get(Calendar.YEAR),
    calendario.get(Calendar.MONTH),
    calendario.get(Calendar.DAY_OF_MONTH)
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF7F5FA))
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp)
      .padding(top = 60.dp, bottom = 40.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      text = "ExpiryAlert",
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF6750A4)
    )

    Spacer(modifier = Modifier.height(30.dp))

    Text(
      text = "Agregar producto",
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Registra la fecha de caducidad",
      fontSize = 16.sp,
      color = Color.Gray
    )

    Spacer(modifier = Modifier.height(30.dp))

    Button(onClick = escanearCodigo, modifier = Modifier.fillMaxWidth()) {
      Text("📷 Escanear código de barras")
    }
    Spacer(modifier = Modifier.height(10.dp))
    OutlinedButton(onClick = escanearFecha, modifier = Modifier.fillMaxWidth()) {
      Text("📅 Leer caducidad con cámara")
    }
    if (codigoBarras.isNotBlank()) {
      Text("Código detectado: $codigoBarras", color = Color.Gray)
    }
    Spacer(modifier = Modifier.height(20.dp))

    OutlinedTextField(
      value = nombre,
      onValueChange = {
        nombre = it
      },
      label = {
        Text("Nombre del producto")
      },
      placeholder = {
        Text("Ejemplo: Leche")
      },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(15.dp))

    OutlinedTextField(
      value = categoria,
      onValueChange = {
        categoria = it
      },
      label = {
        Text("Categoría")
      },
      placeholder = {
        Text("Ejemplo: Lácteos")
      },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(15.dp))

    OutlinedTextField(
      value = cantidad,
      onValueChange = { nuevoValor ->

        if (nuevoValor.all { it.isDigit() }) {
          cantidad = nuevoValor
        }
      },
      label = {
        Text("Cantidad")
      },
      placeholder = {
        Text("Ejemplo: 2")
      },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number
      )
    )

    Spacer(modifier = Modifier.height(15.dp))

    OutlinedTextField(
      value = fechaCaducidad,
      onValueChange = {},
      readOnly = true,
      label = {
        Text("Fecha de caducidad")
      },
      placeholder = {
        Text("Selecciona una fecha")
      },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedButton(
      onClick = {
        selectorFecha.show()
      },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(15.dp)
    ) {

      Text(
        text = "📅 Seleccionar fecha",
        color = Color(0xFF6750A4)
      )
    }

    Spacer(modifier = Modifier.height(30.dp))

    Button(
      onClick = {

        val usuario =
          auth.currentUser

        when {

          usuario == null -> {

            Toast.makeText(
              context,
              "No hay una sesión iniciada",
              Toast.LENGTH_SHORT
            ).show()
          }

          nombre.isBlank() -> {

            Toast.makeText(
              context,
              "Escribe el nombre del producto",
              Toast.LENGTH_SHORT
            ).show()
          }

          categoria.isBlank() -> {

            Toast.makeText(
              context,
              "Escribe una categoría",
              Toast.LENGTH_SHORT
            ).show()
          }

          cantidad.isBlank() -> {

            Toast.makeText(
              context,
              "Escribe la cantidad",
              Toast.LENGTH_SHORT
            ).show()
          }

          cantidad.toIntOrNull() == null ||
            cantidad.toInt() <= 0 -> {

            Toast.makeText(
              context,
              "La cantidad debe ser mayor que 0",
              Toast.LENGTH_SHORT
            ).show()
          }

          fechaCaducidad.isBlank() -> {

            Toast.makeText(
              context,
              "Selecciona la fecha de caducidad",
              Toast.LENGTH_SHORT
            ).show()
          }

          else -> {

            cargando = true

            val producto =
              hashMapOf(
                "nombre" to nombre.trim(),
                "codigoBarras" to codigoBarras,
                "categoria" to categoria.trim(),
                "cantidad" to cantidad.toInt(),
                "fechaCaducidad" to fechaCaducidad,
                "usuarioId" to usuario.uid,
                "correoUsuario" to (usuario.email ?: ""),
                "fechaCreacion" to Timestamp.now()
              )

            db.collection("productos")
              .add(producto)
              .addOnSuccessListener {

                cargando = false

                Toast.makeText(
                  context,
                  "Producto guardado correctamente",
                  Toast.LENGTH_SHORT
                ).show()

                productoGuardado()
              }
              .addOnFailureListener { error ->

                cargando = false

                Toast.makeText(
                  context,
                  "Error al guardar: ${error.localizedMessage}",
                  Toast.LENGTH_LONG
                ).show()
              }
          }
        }
      },
      enabled = !cargando,
      modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6750A4),
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(15.dp)
    ) {

      if (cargando) {

        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = Color.White,
          strokeWidth = 2.dp
        )

      } else {

        Text(
          text = "Guardar producto",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(15.dp))

    TextButton(
      onClick = regresar
    ) {

      Text(
        text = "← Volver",
        color = Color.Gray
      )
    }
  }
}

// ----------------------------------------------------
// EDITAR PRODUCTO
// ----------------------------------------------------

@Composable
fun PantallaEditarProducto(
  producto: Map<String, Any>,
  regresar: () -> Unit,
  productoActualizado: () -> Unit
) {

  val context = LocalContext.current
  val db = FirebaseFirestore.getInstance()
  val usuario = FirebaseAuth.getInstance().currentUser

  val idProducto =
    producto["id"]?.toString() ?: ""

  var nombre by remember(producto) {
    mutableStateOf(
      producto["nombre"]?.toString() ?: ""
    )
  }

  var categoria by remember(producto) {
    mutableStateOf(
      producto["categoria"]?.toString() ?: ""
    )
  }

  var cantidad by remember(producto) {
    mutableStateOf(
      producto["cantidad"]?.toString() ?: ""
    )
  }

  var fechaCaducidad by remember(producto) {
    mutableStateOf(
      producto["fechaCaducidad"]?.toString() ?: ""
    )
  }

  var cargando by remember {
    mutableStateOf(false)
  }

  val calendario =
    Calendar.getInstance()

  // Intentamos iniciar el calendario en la fecha actual del producto.
  try {

    val formato =
      SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
      )

    formato.isLenient = false

    val fecha =
      formato.parse(fechaCaducidad)

    if (fecha != null) {
      calendario.time = fecha
    }

  } catch (_: Exception) {
  }

  val selectorFecha =
    DatePickerDialog(
      context,
      { _, year, month, day ->

        val fechaSeleccionada =
          Calendar.getInstance()

        fechaSeleccionada.set(
          year,
          month,
          day
        )

        val formato =
          SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
          )

        fechaCaducidad =
          formato.format(
            fechaSeleccionada.time
          )
      },
      calendario.get(Calendar.YEAR),
      calendario.get(Calendar.MONTH),
      calendario.get(Calendar.DAY_OF_MONTH)
    )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF7F5FA))
      .verticalScroll(
        rememberScrollState()
      )
      .padding(horizontal = 24.dp)
      .padding(
        top = 60.dp,
        bottom = 40.dp
      ),
    horizontalAlignment =
      Alignment.CenterHorizontally
  ) {

    Text(
      text = "ExpiryAlert",
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF6750A4)
    )

    Spacer(
      modifier = Modifier.height(30.dp)
    )

    Text(
      text = "Editar producto",
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(
      modifier = Modifier.height(8.dp)
    )

    Text(
      text = "Modifica los datos del producto",
      fontSize = 16.sp,
      color = Color.Gray
    )

    Spacer(
      modifier = Modifier.height(30.dp)
    )

    OutlinedTextField(
      value = nombre,
      onValueChange = {
        nombre = it
      },
      label = {
        Text("Nombre del producto")
      },
      modifier =
        Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(
      modifier = Modifier.height(15.dp)
    )

    OutlinedTextField(
      value = categoria,
      onValueChange = {
        categoria = it
      },
      label = {
        Text("Categoría")
      },
      modifier =
        Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(
      modifier = Modifier.height(15.dp)
    )

    OutlinedTextField(
      value = cantidad,
      onValueChange = { nuevoValor ->

        if (
          nuevoValor.all {
            it.isDigit()
          }
        ) {
          cantidad = nuevoValor
        }
      },
      label = {
        Text("Cantidad")
      },
      modifier =
        Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions =
        KeyboardOptions(
          keyboardType =
            KeyboardType.Number
        )
    )

    Spacer(
      modifier = Modifier.height(15.dp)
    )

    OutlinedTextField(
      value = fechaCaducidad,
      onValueChange = {},
      readOnly = true,
      label = {
        Text("Fecha de caducidad")
      },
      modifier =
        Modifier.fillMaxWidth()
    )

    Spacer(
      modifier = Modifier.height(10.dp)
    )

    OutlinedButton(
      onClick = {
        selectorFecha.show()
      },
      modifier =
        Modifier.fillMaxWidth(),
      shape =
        RoundedCornerShape(15.dp)
    ) {

      Text(
        text = "📅 Cambiar fecha",
        color = Color(0xFF6750A4)
      )
    }

    Spacer(
      modifier = Modifier.height(30.dp)
    )

    Button(
      onClick = {

        when {

          usuario == null -> {

            Toast.makeText(
              context,
              "No hay una sesión iniciada",
              Toast.LENGTH_SHORT
            ).show()
          }

          idProducto.isBlank() -> {

            Toast.makeText(
              context,
              "No se encontró el producto",
              Toast.LENGTH_SHORT
            ).show()
          }

          nombre.isBlank() -> {

            Toast.makeText(
              context,
              "Escribe el nombre del producto",
              Toast.LENGTH_SHORT
            ).show()
          }

          categoria.isBlank() -> {

            Toast.makeText(
              context,
              "Escribe una categoría",
              Toast.LENGTH_SHORT
            ).show()
          }

          cantidad.isBlank() -> {

            Toast.makeText(
              context,
              "Escribe la cantidad",
              Toast.LENGTH_SHORT
            ).show()
          }

          cantidad.toIntOrNull() == null ||
            cantidad.toInt() <= 0 -> {

            Toast.makeText(
              context,
              "La cantidad debe ser mayor que 0",
              Toast.LENGTH_SHORT
            ).show()
          }

          fechaCaducidad.isBlank() -> {

            Toast.makeText(
              context,
              "Selecciona una fecha",
              Toast.LENGTH_SHORT
            ).show()
          }

          else -> {

            cargando = true

            val cambios =
              hashMapOf<String, Any>(
                "nombre" to nombre.trim(),
                "categoria" to categoria.trim(),
                "cantidad" to cantidad.toInt(),
                "fechaCaducidad" to fechaCaducidad,
                "usuarioId" to usuario.uid,
                "correoUsuario" to (usuario.email ?: "")
              )

            db.collection("productos")
              .document(idProducto)
              .update(cambios)
              .addOnSuccessListener {

                cargando = false

                Toast.makeText(
                  context,
                  "Producto actualizado correctamente",
                  Toast.LENGTH_SHORT
                ).show()

                productoActualizado()
              }
              .addOnFailureListener { error ->

                cargando = false

                Toast.makeText(
                  context,
                  "Error al actualizar: ${error.localizedMessage}",
                  Toast.LENGTH_LONG
                ).show()
              }
          }
        }
      },

      enabled = !cargando,

      modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),

      colors =
        ButtonDefaults.buttonColors(
          containerColor =
            Color(0xFF6750A4),
          contentColor =
            Color.White
        ),

      shape =
        RoundedCornerShape(15.dp)
    ) {

      if (cargando) {

        CircularProgressIndicator(
          modifier =
            Modifier.size(24.dp),
          color = Color.White,
          strokeWidth = 2.dp
        )

      } else {

        Text(
          text = "Guardar cambios",
          fontSize = 17.sp,
          fontWeight =
            FontWeight.Bold
        )
      }
    }

    Spacer(
      modifier = Modifier.height(15.dp)
    )

    TextButton(
      onClick = regresar
    ) {

      Text(
        text = "← Cancelar",
        color = Color.Gray
      )
    }
  }
}
