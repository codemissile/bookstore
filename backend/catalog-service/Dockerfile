# Usa una imagen base más ligera
FROM python:3.9-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia solo los archivos necesarios primero (mejora la cache de Docker)
COPY requirements.txt /app/

# Instala dependencias antes de copiar el código (optimiza build cache)
RUN pip install --no-cache-dir -r requirements.txt

# Luego copia el código fuente
COPY . /app

# Establece variables de entorno (Kubernetes las sobreescribirá)
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=bookstore
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

# Expone el puerto de la aplicación
EXPOSE 5000

# Comando de ejecución
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:5000", "app:app"]