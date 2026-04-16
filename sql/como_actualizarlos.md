Opciones para mantenerlos actualizados

  Opción 1 — Bajo demanda (manual):
  ./sql/update_sql_dumps.sh
  Listo, tarda segundos. Ejecuta cuando quieras capturar los cambios del día.

  Opción 2 — Cron diario automático (se ejecuta sin que hagas nada):
  # Editar crontab
  crontab -e

  # Agregar esta línea para que corra todos los días a las 11pm:
  0 23 * * * /home/brandon/Proyects/Factugest-Java-Spring-Boot/sql/update_sql_dumps.sh >>
  /tmp/factugest_dump.log 2>&1

  Opción 3 — Alias en terminal (el más cómodo para el día a día):
  # Agregar a ~/.bashrc o ~/.zshrc
  alias factugest-dump='cd /home/brandon/Proyects/Factugest-Java-Spring-Boot && ./sql/update_sql_dumps.sh'
  Después solo escribes factugest-dump desde cualquier lugar.