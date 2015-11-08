# webapp

maven:
exporty, można dodać do ~/.bashrc jak masz, gdzie /opt - to katalog w ktorym wypakujesz pliki

export PATH="/opt/apache-maven-3.3.3/bin:$PATH"
export M2_HOME=/opt/apache-maven-3.3.3

Później w katalogu z plikiem pom.xml używasz komend:

mvn package

to tworzy w katalogu target/ war ktory wrzucasz na localhost:8080

tomcat:

w pliku opt/apache-tomcat-7.0.65/conf/tomcat-users.xml

wklejasz:

 <role rolename="manager-gui"/>
    <user username="admin" password="admin" roles="manager-gui"/>

wtedy możesz na localhost:8080/manager wpisać admin/admin, i w oknie: WAR file to deploy

wybierasz plik z katalogu target/skinp.war klikasz deploy i powinienes miec stronke postawioną
