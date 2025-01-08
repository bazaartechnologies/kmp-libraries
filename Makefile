.PHONY: publish

publish:
#	./gradlew :$(MODULE):detekt
#	./gradlew :$(MODULE):ktlintCheck
	./gradlew :$(MODULE):clean
	./gradlew :$(MODULE):assembleRelease
	./gradlew :$(MODULE):publish
