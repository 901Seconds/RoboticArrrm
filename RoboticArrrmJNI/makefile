CC=gcc
CFLAGS=-c -Wall -fPIC -I$(shell which java)/../../include -I$(SOURCES_DIR)
LDFLAGS=-fPIC -shared

SOURCES_DIR=src/main/c++
OBJECTS_DIR=target/c++
EXECUTABLE=target/classes/roboticarmlib.so

SOURCES=$(shell find '$(SOURCES_DIR)' -type f -name '*.cpp')
OBJECTS=$(SOURCES:$(SOURCES_DIR)/%.cpp=$(OBJECTS_DIR)/%.o)

all: $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

$(OBJECTS): $(SOURCES)
	mkdir -p $(OBJECTS_DIR)
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -rf $(OBJECTS_DIR) $(EXECUTABLE)