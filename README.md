# App de Consulta de Facturas Android

Una aplicación Android para consultar facturas desde una API Laravel. Esta app permite autenticación, selección de empresas y visualización de facturas con paginación.

## 🔧 Configuración Inicial

### 1. Configurar la URL del Backend

**IMPORTANTE**: Antes de usar la app, debes configurar la URL de tu backend local.

Edita el archivo `app/src/main/res/values/config.xml`:

```xml
<string name="api_base_url">http://TU_IP_LOCAL:8000/api/</string>
```

#### Opciones de configuración:

- **Para emulador Android**: `http://10.0.2.2:8000/api/`
- **Para dispositivo físico**: `http://TU_IP_LOCAL:8000/api/` (ej: `http://192.168.1.100:8000/api/`)
- **Para producción**: `https://tu-dominio.com/api/`

### 2. Encontrar tu IP local

**Windows:**
```cmd
ipconfig
```
Busca "Dirección IPv4" en tu adaptador de red activo.

**Mac/Linux:**
```bash
ifconfig | grep inet
```

### 3. Configurar el Backend Laravel

Asegúrate de que tu servidor Laravel esté corriendo con:
```bash
php artisan serve --host=0.0.0.0 --port=8000
```

## 📱 Funcionalidades

- ✅ Login con autenticación JWT
- ✅ Selección de empresas
- ✅ Dashboard con botones de acción
- ✅ Lista de facturas con paginación
- ✅ Detalle de facturas
- ✅ Filtros por estado de factura
- ✅ Navegación intuitiva

## 🏗 Arquitectura

- **MVVM**: ViewModels para lógica de negocio
- **Retrofit**: Cliente HTTP para API
- **Coroutines**: Programación asíncrona
- **RecyclerView**: Listas eficientes con paginación
- **SharedPreferences**: Almacenamiento local seguro

## 🔐 Seguridad

- Tokens JWT para autenticación
- URLs configurables (no hardcodeadas)
- Logs de debug ocultables en producción
- Datos sensibles excluidos del control de versiones

## 🚀 Cómo ejecutar

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Configura la URL del backend en `config.xml`
4. Asegúrate de que tu backend Laravel esté corriendo
5. Ejecuta la app en emulador o dispositivo

## 🔄 Endpoints utilizados

La app consume estos endpoints del backend Laravel:

```
POST /api/mobile/login
GET  /api/mobile/empresas
POST /api/mobile/empresa/cambiar/{id}
GET  /api/mobile/facturas/ventas
GET  /api/mobile/facturas/ventas/{id}
```

## 📋 Requisitos

- Android Studio 4.0+
- Android SDK 24+
- Kotlin 1.8+
- Backend Laravel con API configurada

## 🐛 Solución de problemas

### Error de red
- Verifica que la URL en `config.xml` sea correcta
- Confirma que el backend esté corriendo
- En emulador, usa `10.0.2.2` en lugar de `localhost`

### Error 401
- Verifica las credenciales de login
- Confirma que el backend tenga configuración CORS

### La app se cierra
- Revisa los logs en Android Studio (Logcat)
- Verifica que todos los permisos estén configurados

## 📄 Licencia

Este proyecto es para fines educativos y de desarrollo.
