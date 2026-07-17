# Sistema de Gestión de Almacén Tecnológico 

Aplicación de escritorio desarrollada en JavaFX para la gestión de un negocio. El sistema permite registrar operaciones de inventario, ventas, clientes, proveedores y categorías, con autenticación de usuarios y persistencia en PostgreSQL.

El proyecto fue construido con una organización por capas que separa la interfaz gráfica, la lógica de negocio, el acceso a datos y los modelos de dominio.

## Objetivo del proyecto

El proyecto permite evidenciar la aplicación práctica de patrones de separación de responsabilidades, programación orientada a objetos, conexión a bases de datos relacionales con JDBC y construcción de interfaces gráficas con JavaFX.

## Funciones

- Gestión estructurada del inventario.
- Registro y consulta de ventas.
- Administración de clientes y proveedores.
- Clasificación de productos por categorías.
- Visualización de indicadores relevantes mediante un dashboard.
- Acceso mediante usuarios autenticados.

## Arquitectura

El proyecto sigue una arquitectura por capas inspirada en el patrón MVC y en la separación clásica entre presentación, lógica de negocio y persistencia.

```text
Interfaz JavaFX / FXML
        ↓
Controladores
        ↓
ViewModels, DTOs y servicios asíncronos
        ↓
Servicios de negocio
        ↓
DAOs
        ↓
PostgreSQL
```

### Capa de presentación

Contiene las vistas FXML, hojas de estilo CSS y controladores JavaFX. Esta capa se encarga de la interacción con el usuario, navegación entre módulos, formularios, tablas, diálogos y componentes visuales.

### Capa de controladores

Los controladores coordinan eventos de la interfaz, cargan vistas, invocan servicios y muestran resultados o mensajes de error. Ejemplos representativos son los controladores de autenticación, dashboard, productos, ventas, clientes, proveedores y categorías.

### Capa de servicios

Los servicios encapsulan reglas de negocio, validaciones y operaciones de alto nivel. Se definen mediante interfaces y se implementan en clases concretas para favorecer la separación entre contrato y comportamiento.

### Capa DAO

Los DAOs concentran el acceso a PostgreSQL mediante JDBC. Esta capa ejecuta consultas SQL, transforma resultados en modelos de dominio y centraliza errores de persistencia mediante excepciones específicas.

### Modelos, DTOs y ViewModels

- Los modelos representan entidades persistentes como productos, clientes, proveedores, categorías y ventas.
- Los DTOs transportan datos compuestos entre capas, especialmente cuando una vista necesita información combinada de varias tablas.
- Los ViewModels adaptan los datos al formato requerido por la interfaz JavaFX.

## Tecnologías utilizadas

| Tecnología | Uso principal |
| --- | --- |
| Java  | Lenguaje y plataforma base del proyecto |
| JavaFX  | Construcción de la interfaz gráfica de escritorio |
| FXML | Definición declarativa de vistas |
| CSS | Estilización visual de la aplicación |
| Maven | Gestión de dependencias y ciclo de construcción |
| PostgreSQL | Base de datos relacional |
| JDBC | Acceso programático a datos |
| HikariCP | Pool de conexiones a base de datos |
| BCrypt | Hash seguro de contraseñas |

## Estructura del repositorio

```text
.
├── pom.xml
├── README.md
├── nbactions.xml
└── src
    └── main
        ├── java
        │   ├── module-info.java
        │   └── com/mvcjava/sagt/javafx
        │       ├── App.java
        │       ├── async
        │       ├── auth
        │       ├── config
        │       ├── controller
        │       ├── dao
        │       ├── dto
        │       ├── enums
        │       ├── exception
        │       ├── filter
        │       ├── service
        │       ├── util
        │       └── viewmodel
        └── resources
            └── com/mvcjava/sagt/javafx
                ├── database.properties
                └── view
                    ├── *.fxml
                    └── styles
```

## Autor

Desarrollado por [Lucas Durán](https://github.com/lucasduran3) como proyecto de práctica y portfolio.
