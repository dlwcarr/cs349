default:
	javac -d classes -cp :jars/com/google/gson/gson-2.2.2.jar src/*.java

run: default
	java -cp classes:jars/com/google/gson/gson-2.2.2.jar com.sketch.AppFrame

clean:
	rm -rf classes/com/sketch/*.class