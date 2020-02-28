##Create the Java classes starting from YANG model(s) available in the yang dir under src/main
mvn clean install -Denforcer.skip=true -Dmaven.test.skip=true

##Create and then move the dirs in order to build the jar that will contain the QAMeleonIM
mkdir target/generated-sources/mdsal-binding/src; mkdir target/generated-sources/mdsal-binding/src/main; mkdir target/generated-sources/mdsal-binding/src/main/java
mv target/generated-sources/mdsal-binding/org/ target/generated-sources/mdsal-binding/src/main/java/

#Use an ad-hoc pom in order to build the jar
cp pom_generated.xml ./target/generated-sources/mdsal-binding/pom.xml

##The jar is built with QAMeleonIM. The artifact id is it.nextworks.QAMeleon
cd ./target/generated-sources/mdsal-binding/
mvn clean install

##To include the generated lib:
#    <dependency>
#            <groupId>it.nextworks.QAMeleon.InformationModel</groupId>
#            <artifactId>QAMeleonIM</artifactId>
#            <version>0.0.1-SNAPSHOT</version>
#        </dependency>
#
