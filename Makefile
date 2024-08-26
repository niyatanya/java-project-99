run-dist: #запуск программы
	./build/install/app/bin/app

install:
	./gradlew installDist

start:
	./gradlew run

dev:
	./gradlew run --args='--spring.profiles.active=development'

build:
	./gradlew clean
	./gradlew build

clean:
	./gradlew clean

test:
	./gradlew clean
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

build-run: build run

.PHONY: build