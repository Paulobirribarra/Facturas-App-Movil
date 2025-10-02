# Configuración de Seguridad - IMPORTANTE

## ⚠️ ANTES DE SUBIR A GITHUB

### 1. Archivos que NUNCA debes subir:
- `local.properties` - Contiene rutas específicas de tu máquina
- `*.json` - Archivos con datos de prueba (pueden contener RUTs, emails, etc.)
- `keystore.properties` - Configuración de certificados
- `*.jks`, `*.keystore` - Certificados de firma

### 2. Datos sensibles encontrados en tu proyecto:
- ❌ IP privada hardcodeada: `192.168.32.1`
- ❌ Archivos JSON con datos reales de empresas y facturas
- ❌ Logs que muestran tokens completos

### 3. ✅ Soluciones implementadas:
- Configuración de URLs desde recursos XML
- Logs de tokens censurados (solo muestran últimos 8 caracteres)
- `.gitignore` mejorado para Android
- Documentación para configuración local

## 🔧 Configuración para diferentes entornos

### config.xml para DESARROLLO LOCAL:
```xml
<string name="api_base_url">http://10.0.2.2:8000/api/</string>
<bool name="debug_mode">true</bool>
<bool name="enable_http_logs">true</bool>
```

### config.xml para DISPOSITIVO FÍSICO:
```xml
<string name="api_base_url">http://192.168.1.XXX:8000/api/</string>
<bool name="debug_mode">true</bool>
<bool name="enable_http_logs">true</bool>
```

### config.xml para PRODUCCIÓN:
```xml
<string name="api_base_url">https://tu-dominio.com/api/</string>
<bool name="debug_mode">false</bool>
<bool name="enable_http_logs">false</bool>
```

## 📋 Checklist antes de subir a GitHub:

- [ ] Eliminar archivos *.json del directorio del proyecto
- [ ] Configurar URL genérica en config.xml
- [ ] Verificar que local.properties esté en .gitignore
- [ ] Remover logs con datos sensibles
- [ ] Actualizar README con instrucciones

## 🚀 Comandos para limpiar el proyecto:

```bash
# Eliminar archivos con datos sensibles
rm *.json
rm app/*.json

# Verificar qué archivos se subirán
git status

# Agregar solo archivos seguros
git add .
git commit -m "Initial commit - clean version"
```
