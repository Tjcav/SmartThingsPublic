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
		input "door1", "capability.Door Control", title: "Which?"
	}
    section("Select a Garage Door") {
		input "door2", "capability.Door Control", title: "Which?"
	}

	section("Close after...") {
		input "maxOpenTime", "number", title: "Minutes?"
	}
}

def installed()
{
	subscribe(door1, "door", doorHandler)
	subscribe(door2, "door", doorHandler)
    initialize()
}

def initialize()
{
	state.status = [:]
    state.status[door1.getId()] = "null"
    state.status[door2.getId()] = "null"
}

def updated()
{
	unsubscribe()
	subscribe(door1, "door", doorHandler)
    subscribe(door2, "door", doorHandler)
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
	def device = evt.getDevice()
	def status = currentStatus(device, "door")
    log.debug "Door Status: $status"
    def isNotScheduled = state.status[evt.device.getId()] != "scheduled"
	def isOpen = status == "open"

	if (!isopen) {
        clearStatus(device)
    }

    if (isOpen && isNotScheduled) {
        if (device.getId() == door1.getId()) {
        	runIn(maxOpenTime * 60, takeAction1, [overwrite: true])
            state.status[device.getId()] = "scheduled"
            log.debug "Scheduled to close $device in $maxOpenTime min."
        }
        else if (device.getId() == door2.getId()) {
        	runIn(maxOpenTime * 60, takeAction2, [overwrite: true])
            state.status[device.getId()] = "scheduled"
	        log.debug "Scheduled to close $device in $maxOpenTime min."
        }
        else {
        	log.warn "Device not found"
        }
    }
}

def takeAction1(){
	takeAction(door1)
}

def takeAction2(){
	takeAction(door2)
}

def takeAction(device){
	log.trace "Take Action"
	if (state.status[device.getId()] == "scheduled")
	{
    	log.trace "Close Door"
    	device.close()
	} else {
		log.trace "Status is no longer scheduled."
	}
}

def clearStatus(device) {
    state.status[device.getId()] = "null"
}