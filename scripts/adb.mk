# App package name (common variable)
APP_PACKAGE = com.tech.bazaar.kmp.app
APP_ACTIVITY = .MainActivity
APP_GPS_LATITUDE = 24.8218658
APP_GPS_LONGITUDE = 67.0324239

# Task to install ADB if not already installed
adb_install:
	@which adb >/dev/null 2>&1 || brew install android-platform-tools
	@echo "ADB is installed."

# Guardrail task to check if a device is connected
adb_check: adb_install
	@adb devices | grep -w "device" || (echo "No device/emulator found. Please connect one." && exit 1)

adb_stop: adb_check
	@adb shell am force-stop $(APP_PACKAGE)

adb_launch: adb_check
	@adb shell am start -n $(APP_PACKAGE)/$(APP_ACTIVITY)
# 	@adb shell monkey -p $(APP_PACKAGE) -c android.intent.category.LAUNCHER 1

# task to set location to specified coordinates
adb_locate: adb_check
	@echo "Resetting GPS and setting location to specified coordinates..."
	@$(MAKE) adb_stop
	@adb shell settings put secure location_providers_allowed -gps
	@sleep 1
	@adb shell settings put secure location_providers_allowed +gps
	@adb emu geo fix $(APP_GPS_LONGITUDE) $(APP_GPS_LATITUDE)
	@sleep 1
	@adb shell am broadcast -a android.intent.action.LOCATION_MODE_CHANGED
	@adb shell settings put secure location_mode 0
	@sleep 1
	@adb shell settings put secure location_mode 3
	@sleep 1
	@$(MAKE) adb_launch
	@echo "Location set to ($(APP_GPS_LATITUDE), $(APP_GPS_LONGITUDE)) and app restarted."

# Task to clear app storage and restart the app
adb_refresh: adb_check
	@echo "Clearing app storage and restarting the app..."
	@adb shell pm clear $(APP_PACKAGE)
	@$(MAKE) adb_launch
	@echo "App storage cleared and app restarted."

# Task to trigger a deep link with argument
adb_deeplink: adb_check
	@if [ -z "$(DEEPLINK)" ]; then \
		echo "Error: Please provide a deep link using DEEPLINK=<url>"; \
		exit 1; \
	fi
	@echo "Triggering deep link: $(DEEPLINK)"
	@adb shell am start -W -a android.intent.action.VIEW -d "$(DEEPLINK)" $(APP_PACKAGE)
	@echo "Deep link triggered: $(DEEPLINK)"

# Task to uninstall the app
adb_uninstall: adb_check
	@echo "Uninstalling the app..."
	@adb uninstall $(APP_PACKAGE)
	@echo "App $(APP_PACKAGE) has been uninstalled."

# Task to list files in the app's data directory
adb_files: adb_check
	@echo "Listing files in the app's data directory..."
	@adb shell run-as $(APP_PACKAGE) ls -la /data/data/$(APP_PACKAGE)
	@echo "File listing completed."

# Task to start an interactive shell in the app's data directory
adb_shell: adb_check
	@echo "Starting interactive shell in the app's data directory..."
	@adb shell run-as $(APP_PACKAGE) sh
	@echo "Exited interactive shell."

adb_wifi_off: adb_check
	adb shell svc wifi disable

adb_wifi_on: adb_check
	adb shell svc wifi enable

adb_data_on: adb_check
	adb shell svc data enable

adb_data_off: adb_check
	adb shell svc data disable

adb_network_off: adb_wifi_off adb_data_off

adb_network_on: adb_wifi_on adb_data_on

adb_fingerprint: adb_check
	@echo "Fetching device fingerprint..."
	@adb shell getprop ro.build.fingerprint
	@echo "Copy this fingerprint into Firebase Console to allow Firestore and Remote Config access."