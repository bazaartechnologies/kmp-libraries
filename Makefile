.PHONY: publish

publish:
#	./gradlew :$(mod):detekt
#	./gradlew :$(mod):ktlintCheck
	./gradlew :$(mod):clean
	./gradlew :$(mod):assembleRelease
	./gradlew :$(mod):publish

include scripts/adb.mk