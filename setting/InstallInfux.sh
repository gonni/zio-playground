mkdir influxdb_data
docker run \
	-d \
	--name influxdb23 \
      -p 8086:8086 \
      -v influxdb_data:/var/lib/influxdb2 \
      influxdb:2.3.0