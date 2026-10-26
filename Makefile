# Generieke build voor alle mods in de root.
# Een mod = een map met een gradlew. Afwijkingen per mod in <mod>/.modbuild (bv. TASK=assemble).

MODS := $(patsubst %/gradlew,%,$(wildcard */gradlew))
MOD  ?=

ifneq ($(MOD),)
-include $(MOD)/.modbuild
endif

TASK ?= build

COMPOSE := HOST_UID=$(shell id -u) HOST_GID=$(shell id -g) MOD=$(MOD) docker compose
RUN     := mkdir -p .cache/gradle && $(COMPOSE) run --rm build

.PHONY: help list image build build-all gradle shell clean check-mod

help:
	@echo "make list                     - toon alle mods"
	@echo "make build MOD=<mod>          - build mod, jars naar dist/<mod>/"
	@echo "make build-all                - build alle mods"
	@echo "make gradle MOD=<mod> TASK=.. - willekeurige gradle-taak (bv. TASK=spotlessApply)"
	@echo "make shell MOD=<mod>          - shell in de build-container"
	@echo "make clean MOD=<mod>          - gradle clean + dist/<mod> leegmaken"
	@echo "make image                    - (her)bouw het build-image"

list:
	@for m in $(MODS); do echo "$$m"; done

image:
	$(COMPOSE) build

check-mod:
ifeq ($(MOD),)
	$(error MOD is verplicht, bv. make build MOD=GregTech-Modern)
endif
	@test -x "$(MOD)/gradlew" || { echo "Geen uitvoerbare $(MOD)/gradlew"; exit 1; }

build: check-mod
	$(RUN) ./gradlew $(TASK)
	@mkdir -p dist/$(MOD)
	@find "$(MOD)/build/libs" -maxdepth 1 -name '*.jar' \
		! -name '*-dev*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' \
		-exec cp -v {} dist/$(MOD)/ \;

build-all:
	@for m in $(MODS); do $(MAKE) --no-print-directory build MOD=$$m || exit 1; done

gradle: check-mod
	$(RUN) ./gradlew $(TASK)

shell: check-mod
	$(RUN) bash

clean: check-mod
	$(RUN) ./gradlew clean
	rm -rf dist/$(MOD)
