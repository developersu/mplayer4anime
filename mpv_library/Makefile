# Compiler
CC=gcc
# Flags
CFLAGS=-O2
MKDIR_P = mkdir -p
APP_NAME = mpvjni

all: x86 amd64

x86:
	$(MKDIR_P) ./x86
	$(CC) ${CFLAGS} -m32 -c -fPIC -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" ${APP_NAME}.c -o ${APP_NAME}_x86.o
	$(CC) ${CFLAGS} -m32 -shared -fPIC -o ./x86/${APP_NAME}.so ${APP_NAME}_x86.o -lc

amd64:
	$(MKDIR_P) ./amd64
	$(CC) ${CFLAGS} -m64 -c -fPIC -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" ${APP_NAME}.c -o ${APP_NAME}_amd64.o
	$(CC) ${CFLAGS} -m64 -shared -fPIC -o ./amd64/${APP_NAME}.so ${APP_NAME}_amd64.o -lc

clean:
	rm -rf \
		${APP_NAME}_amd64.o \
		${APP_NAME}_x86.o \
		./x86 \
		./amd64

headers: 
	cd /src/main/java 
	javac mplayer4anime/mpv/MpvJni.java -h ../../../mpv_library/

install: x86 amd64
	install ./x86/${APP_NAME}.so ../src/main/resources/native/linux/x86/
	install ./amd64/${APP_NAME}.so ../src/main/resources/native/linux/amd64/

uninstall:
	rm ../src/main/resources/native/linux/x86/${APP_NAME}.so
	rm ../src/main/resources/native/linux/amd64/${APP_NAME}.so
