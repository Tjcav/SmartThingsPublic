/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Garage Door Monitor
 *
 *  Author: SmartThings
 */
definition(
    name: "Garage Door Monitor",
    namespace: "Tjcav",
    author: "Thomas Cavaliere",
    description: "Monitor garage door and close it if it is open too long",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {
	section("Select a Garage Door") {
		input "door", "capability.Door Control", title: "Which?"
	}
    //TODO: add a second garage door
	section("Close after...") {
		input "maxOpenTime", "number", title: "Minutes?"
	}
}

def installed()
{
	subscribe(door, "door", doorHandler)
}

def updated()
{
	unsubscribe()
	subscribe(door, "door", doorHandler)
}

private currentStatus(devices, attribute) {
	log.trace "currentStatus($devices, $attribute)"
	def result = null
    def map = [:]
    devices.each {
        def value = it.currentValue(attribute)
        map[value] = (map[value] ?: 0) + 1
        log.trace "$it.displayName: $value"
    }
    log.trace map
    result = map.collect{it}.sort{it.value}[-1].key
    log.debug "$attribute = $result"
    result
}

def doorHandler(evt) {

	def status = currentStatus(door, "door")
    log.debug "Door Status: $status"
    def isNotScheduled = state.status != "scheduled"
	def isOpen = status == "open"

	if (!isopen) {
        clearStatus()
    }

    if (isOpen && isNotScheduled) {
        runIn(maxOpenTime * 60, takeAction, [overwrite: false])
        state.status = "scheduled"
        log.debug "Scheduled to close $door in $maxOpenTime min."
    }
}

def takeAction(){
	log.trace "Take Action"
	if (state.status == "scheduled")
	{
    	log.trace "Close Door"
    	door.close()
	} else {
		log.trace "Status is no longer scheduled."
	}
}

def clearStatus() {
	state.status = null
}