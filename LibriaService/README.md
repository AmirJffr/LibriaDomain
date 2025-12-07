MySql BDD :

après avoir céer le fichier persistence.xml et ajouter la partie mysql dans docker compose, il faut créer manuellement la pool dans JDBC


Demarer les conteneur :

docker compose up -d


1.
docker exec -it projetsoftware-payara-libria-service-1 asadmin \
create-jdbc-connection-pool \
--restype javax.sql.DataSource \
--datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
--property serverName=libria-mysql:portNumber=3306:databaseName=libria:user=libria:password=libria:useSSL=false:allowPublicKeyRetrieval=true \
LibriaPool

2.
docker exec -it projetsoftware-payara-libria-service-1 asadmin \
create-jdbc-resource \
--connectionpoolid LibriaPool \
jdbc/LibriaDS

3.
docker exec -it projetsoftware-payara-libria-service-1 asadmin ping-connection-pool LibriaPool

4.
s'assurer que l'app et le war sont deployer sur payara

docker exec -it projetsoftware-payara-libria-service-1 \
ls -l /opt/payara/appserver/glassfish/domains/domain1/autodeploy

5. (si failed)
deployer le war manuellement du service apres avoir cérer les pool et DS :

5.1
docker exec -it projetsoftware-payara-libria-service-1 asadmin \
deploy --force=true \
/opt/payara/appserver/glassfish/domains/domain1/autodeploy/LibriaService.war