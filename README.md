# Ewyc

Aplicación para dispositivos móviles del sistema Ewyc destinada a los clientes de los establecimientos. Esta aplicación forma parte del sistema Ewyc "Evolve With Your Capacity": Sistema para el control de aforos del Trabajo Final del Máster en Ingeniería Informática de la Universidad de A Coruña.


## Guía de instalación

Para poder instalar de forma correcta esta aplicación es necesario seguir los pasos que se detallan a continuación:

1. **Modificar la dirección en la que se encuentra la aplicación del sistema:** está dirección se encuentra definida en el atributo BASE_ENDPOINT en el archivo [BackEndEndpoints.java](ClientsApp/app/src/main/java/es/ewic/clients/utils/BackEndEndpoints.java).
2. **Añadir el dominio a la configuración de seguridad de la red para no bloquear peticiones:** también es necesario añadir el dominio en el archivo [network_security_config.xml](ClientsApp/app/src/main/res/xml/network_security_config.xml) para evitar que la aplicación nos bloquee las peticiones a esa dirección.
3. **Generar y instalar el APK**.

## Defensa

Para la defensa de este TFM se utilizará el APK ([ewyc.apk](ewyc.apk)) ya configurado que se encuentra en el directorio raíz de este proyecto.
