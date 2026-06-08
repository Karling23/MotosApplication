<div align="center">

<img src="https://ute.edu.ec/wp-content/uploads/2021/08/LogoUteTrans.png" alt="UTE - Escuela de Tecnologías" width="250"/>

</div>

<hr>
<br>

<div style="border-left: 4px solid #1e88e5; padding-left: 15px; margin-top: 20px;">

<p><strong>Universidad Tecnológica Equinoccial</strong></p>

<p><strong>Escuela de Tecnologías</strong></p>

<p><strong>Carrera:</strong> Desarrollo de Software</p>

<p><strong>Asignatura:</strong> Programación IV</p>

</div>

<br><br>

<p><strong>Tema:</strong> Construcción de Móvil Kotlin.</p>

<br>

<p><strong>Fecha:</strong> 07/06/2026</p>

<p><strong>Presentado por:</strong></p>

<ul>
  <li>Alquinga Carlos</li>
</ul>

<p><strong>Docente:</strong> Francisco Javier Higuera González </p>

<hr>

# Venta de motos - MotosApplication (APP móvil)

**MotosApp** es una aplicación móvil de comercio electrónico especializada en motocicletas y equipo de moto. Cuenta con dos roles de usuario:

- **Cliente**: puede explorar el catálogo, ver detalles de productos, agregar artículos al carrito, realizar pedidos y revisar su historial de órdenes.
- **Administrador (staff)**: accede a un panel de administración completo para gestionar motocicletas, cascos, accesorios, marcas, categorías, pedidos y usuarios.

La app consume una API REST Django (con autenticación JWT) y monitorea en tiempo real el estado de la conexión con el backend.

---

## ⚙️ Requisitos de instalación

### Herramientas necesarias

| Herramienta | Versión mínima |
|---|---|
| Android Studio | Hedgehog (2023.1.1) o superior |
| JDK (incluido con Android Studio) | 17+ |
| Android SDK | API 26 (Android 8.0 Oreo) |
| Gradle | 8.x (wrapper incluido) |
| Dispositivo / Emulador | API 26+ |

### Dependencias principales (auto-descargadas por Gradle)

| Librería | Versión |
|---|---|
| Kotlin | 2.0.21 |
| Jetpack Compose BOM | 2024.10.01 |
| Hilt (DI) | 2.52 |
| Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Navigation Compose | 2.8.3 |
| Coil (imágenes) | 2.7.0 |
| DataStore Preferences | 1.1.1 |
| KSP | 2.0.21-1.0.28 |

---

## 🔗 Configuración de la URL base del backend

### Opción 1 — Por defecto (emulador Android)

La URL base por defecto apunta al backend corriendo en `localhost` del PC host:

```
http://10.0.2.2:8000/api/
```

Esta configuración viene lista para usar cuando el backend corre localmente en el mismo equipo.

### Opción 2 — Personalizar con `local.properties`

Para apuntar a otro servidor (por ejemplo, una IP de red local o un servidor remoto), agrega la siguiente línea al archivo `local.properties` en la raíz del proyecto:

```properties
API_BASE_URL=http://alquinga-motos.uaeftt-ute.site/api/
```

> **Nota:** `local.properties` no se sube a control de versiones y es específico de cada equipo.

## 👤 Usuario y contraseña de prueba

### Administrador (Staff)

| Campo | Valor |
|---|---|
| Usuario | `admin` |
| Contraseña | `Admin1234!` |
| Rol | Administrador (acceso al panel admin) |

### Cliente

| Campo | Valor |
|---|---|
| Usuario | `Carlos` |
| Contraseña | `carlos1234` |
| Rol | Cliente (acceso a tienda y pedidos) |

> También puedes crear una cuenta desde la pantalla de **Registro** dentro de la app.

---

## 🗂️ Entidades implementadas

La aplicación gestiona **7 entidades** que se persisten en el backend y consumen mediante la API REST:

### 1. 🏍️ Motocicleta (`/api/motocicletas/`)
Producto principal de la tienda. Representa cada modelo de motocicleta disponible para su venta.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Int | Identificador único |
| `marca` | Int (FK) | Referencia a la marca del fabricante |
| `categoria` | Int (FK) | Categoría a la que pertenece |
| `modelo` | String | Nombre del modelo |
| `anio` | Int | Año de fabricación |
| `cilindrada` | Int | Cilindrada del motor (cc) |
| `color` | String | Color disponible |
| `precio` | String | Precio de venta |
| `stock` | Int | Unidades disponibles |
| `is_active` | Boolean | Si está publicada en la tienda |
| `descripcion` | String | Descripción detallada |
| `imagen` | String? | URL de la imagen |

---

### 2. 🪖 Casco (`/api/cascos/`)
Accesorio de seguridad. Complementa el catálogo con cascos certificados.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Int | Identificador único |
| `marca` | Int (FK) | Referencia a la marca |
| `modelo` | String | Nombre del modelo |
| `talla` | String | Talla (XS, S, M, L, XL, XXL) |
| `color` | String | Color del casco |
| `certificacion` | String | Norma de seguridad (DOT, ECE 22.06, etc.) |
| `precio` | String | Precio de venta |
| `stock` | Int | Unidades disponibles |
| `is_active` | Boolean | Si está publicado |
| `descripcion` | String | Descripción detallada |
| `imagen` | String? | URL de la imagen |

---

### 3. 🧤 Accesorio (`/api/accesorios/`)
Artículos complementarios: guantes, chaquetas, botas, etc.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Int | Identificador único |
| `nombre` | String | Nombre del accesorio |
| `marca` | Int (FK) | Referencia a la marca |
| `categoria_accesorio` | String | Subcategoría (guante, bota, chaqueta...) |
| `precio` | String | Precio de venta |
| `stock` | Int | Unidades disponibles |
| `is_active` | Boolean | Si está publicado |
| `descripcion` | String | Descripción detallada |
| `imagen` | String? | URL de la imagen |

---

### 4. 🏭 Marca (`/api/marcas/`)
Fabricantes o marcas de los productos (ej. Honda, Yamaha, Shoei).

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Int | Identificador único |
| `nombre` | String | Nombre de la marca |
| `pais_origen` | String? | País de origen |
| `descripcion` | String? | Descripción de la marca |
| `activo` | Boolean | Si la marca está activa |

---

### 5. 📂 Categoría (`/api/categorias/`)
Clasificación de motocicletas (ej. Deportiva, Custom, Enduro, Semi-Deportiva).

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Int | Identificador único |
| `nombre` | String | Nombre de la categoría |
| `descripcion` | String? | Descripción de la categoría |
| `activo` | Boolean | Si está activa |

---

### 6. 📦 Pedido (`/api/pedidos/`)
Orden de compra creada por un cliente. Agrupa uno o más productos.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Int | Identificador único |
| `usuario` | Int (FK) | Usuario que realizó el pedido |
| `estado` | String | Estado actual (`pendiente`, `confirmado`, `enviado`, `entregado`, `cancelado`) |
| `total` | Double | Monto total del pedido |
| `direccion_envio` | String | Dirección de entrega |

---

### 7. 👤 Usuario (`/api/users/`)
Cuentas registradas en el sistema. Pueden ser clientes o administradores.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Int | Identificador único |
| `username` | String | Nombre de usuario |
| `email` | String | Correo electrónico |
| `firstName` | String | Nombre |
| `lastName` | String | Apellido |
| `telefono` | String? | Teléfono de contacto |
| `direccion` | String? | Dirección por defecto |
| `isStaff` | Boolean | Si tiene permisos de administrador |
| `isActive` | Boolean | Si la cuenta está activa |
| `numOrders` | Int | Número de pedidos realizados |

---

## 📱 Listado de pantallas

### 🔐 Autenticación (sin sesión requerida)

| Pantalla | Descripción |
|---|---|
| **Login** | Inicio de sesión con usuario y contraseña |
| **Registro** | Creación de nueva cuenta de cliente |

---

### 🛍️ Área pública / Cliente

| Pantalla | Descripción |
|---|---|
| **Home** | Página de inicio con destacados y acceso rápido al catálogo |
| **Catálogo** | Exploración de motocicletas, cascos y accesorios con búsqueda y filtros |
| **Detalle de producto** | Vista completa de un artículo con imágenes, precio y botón de agregar al carrito |
| **Carrito** | Bottom sheet con el resumen del pedido actual y opción de confirmar compra |
| **Mis pedidos** | Historial de órdenes del cliente autenticado |
| **Detalle de pedido** | Información completa de un pedido: estado, artículos y total |
| **Perfil** | Datos del usuario autenticado y acceso al panel admin (si es staff) |

---

### 🛠️ Panel de administración (solo staff)

| Pantalla | Descripción |
|---|---|
| **Dashboard** | Vista general con KPIs: motocicletas, marcas, pedidos, usuarios, facturación y estado de la API |
| **Motocicletas Admin** | CRUD completo: listar, buscar, filtrar por stock, agregar, editar y eliminar motocicletas |
| **Cascos Admin** | CRUD completo de cascos |
| **Accesorios Admin** | CRUD completo de accesorios |
| **Marcas Admin** | CRUD completo de marcas |
| **Categorías Admin** | CRUD completo de categorías |
| **Pedidos Admin** | Listado de todos los pedidos con opción de cambiar estado |
| **Detalle pedido Admin** | Vista detallada de un pedido con selector de estado |
| **Usuarios Admin** | Gestión de cuentas: listar, filtrar, crear, editar y activar/desactivar |

---

## 🔌 Ejemplos de consumo de la API con token

Todos los endpoints protegidos requieren el header:

```
Authorization: Bearer <access_token>
```

### 1. Autenticación — Obtener token

```http
POST /api/auth/login/
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin1234!"
}
```

**Respuesta:**
```json
{
  "access": "eyJhbGciOiJIUzI1NiIsInR5...",
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5...",
  "user_id": 1,
  "username": "admin",
  "email": "admin@motosapp.com",
  "is_staff": true
}
```

---

### 2. Verificar salud del servidor

```http
GET /api/health/
```

**Respuesta (200 OK):**
```json
{ "status": "ok" }
```

---

### 3. Listar motocicletas (con búsqueda)

```http
GET /api/motocicletas/?search=Honda&page=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...
```

**Respuesta:**
```json
{
  "count": 12,
  "next": null,
  "previous": null,
  "results": [
    {
      "id": 1,
      "marca": 2,
      "categoria": 1,
      "modelo": "CB 500F",
      "anio": 2023,
      "cilindrada": 471,
      "color": "Rojo",
      "precio": "7500.00",
      "stock": 5,
      "is_active": true,
      "descripcion": "Naked sport de media cilindrada",
    }
  ]
}
```

---

### 4. Crear una motocicleta (admin)

```http
POST /api/motocicletas/
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...
Content-Type: application/json

{
  "marca": 2,
  "categoria": 1,
  "modelo": "CBR 600RR",
  "anio": 2024,
  "cilindrada": 599,
  "color": "Negro",
  "precio": "12500.00",
  "stock": 3,
  "is_active": true,
  "descripcion": "Supersport con motor inline-4"
}
```

---

### 5. Obtener perfil del usuario autenticado

```http
GET /api/users/profile/
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...
```

**Respuesta:**
```json
{
  "id": 5,
  "username": "cliente1",
  "email": "cliente1@gmail.com",
  "first_name": "Luis",
  "last_name": "García",
  "is_staff": false,
  "is_active": true
}
```

---

### 6. Crear un pedido

```http
POST /api/pedidos/
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...
Content-Type: application/json

{
  "usuario": 5,
  "estado": "pendiente",
  "total": 7500.00,
  "direccion_envio": "Av. Principal 123, Ciudad de México"
}
```

---

### 7. Actualizar estado de un pedido (admin)

```http
PATCH /api/pedidos/12/
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...
Content-Type: application/json

{
  "estado": "enviado"
}
```

---

### 8. Refrescar el token de acceso

```http
POST /api/auth/token/refresh/
Content-Type: application/json

{
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5..."
}
```

**Respuesta:**
```json
{
  "access": "eyJhbGciOiJIUzI1NiIsInR5... (nuevo token)",
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5... (rotado)"
}
```

---

## 🚀 Instrucciones para ejecutar la app

### Paso 1 — Clonar el repositorio

```bash
git clone https://github.com/Karling23/MotosApplication.git
cd MotosApplication
```

### Paso 2 — Verificar el JDK

El proyecto requiere **JDK 17 o superior**. Android Studio incluye su propio JDK en:

```
C:\Program Files\Android\Android Studio\jbr\
```

Si al compilar aparece el error `"Dependency requires at least JVM runtime version 11"`, agrega esta línea a `gradle.properties`:

```properties
org.gradle.java.home=C\:\\Program Files\\Android\\Android Studio\\jbr
```

### Paso 3 — Configurar la URL del backend (opcional)

Si el backend **no** corre en `localhost:8000`, edita `local.properties`:

```properties
API_BASE_URL=http://alquinga-motos.uaeftt-ute.site/api/
```

### Paso 4 — Abrir en Android Studio

1. Abre **Android Studio**
2. Selecciona `File > Open` y elige la carpeta `MotosApplication`
3. Espera a que Gradle sincronice las dependencias (~2-5 minutos la primera vez)

### Paso 5 — Iniciar un emulador o conectar un dispositivo

- **Emulador**: crea un AVD con API 26+ desde `Tools > Device Manager`
- **Dispositivo físico**: activa la depuración USB en `Ajustes > Opciones de desarrollador`

### Paso 6 — Compilar y ejecutar

**Desde Android Studio:**
Presiona el botón **▶ Run** (Shift+F10)

**Desde la terminal (línea de comandos):**

```bash
# Windows
.\gradlew assembleDebug
.\gradlew installDebug

# Linux / macOS
./gradlew assembleDebug
./gradlew installDebug
```

El APK generado se encontrará en:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🏗️ Arquitectura del proyecto

```
app/src/main/java/com/motosapp/
├── data/
│   ├── local/          # DataStore (persistencia de tokens JWT)
│   ├── remote/
│   │   ├── api/        # Interfaces Retrofit por entidad
│   │   ├── dto/        # Data Transfer Objects
│   │   └── interceptor/# Auth & Token refresh interceptors
│   └── repository/     # Implementaciones de repositorios
├── di/                 # Módulos Hilt (NetworkModule, RepositoryModule)
├── domain/
│   ├── model/          # Entidades del dominio
│   └── repository/     # Interfaces de repositorio (contratos)
├── presentation/
│   ├── components/     # Componentes reutilizables
│   ├── navigation/     # NavGraph y Screen routes
│   ├── ui/             # Pantallas por sección (auth, admin, client, public)
│   └── viewmodel/      # ViewModels con StateFlow
└── theme/              # Paleta de colores, tipografía y formas
```

---


