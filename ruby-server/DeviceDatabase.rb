require 'sqlite3'
require 'json'

class DeviceDatabase
	def initialize()
		begin
			@db = SQLite3::Database.open "Smarthome.db"
		rescue SQLite3::Exception => e
			puts "SQL ERROR: " 
			puts e
		end
	end

	def close()
		begin
			@db.close if @db
		rescue SQLite3::Exception => e
			puts "SQL ERROR: "
			puts e
		end
	end

	
	# SETTER DATABASE FUNCTIONS
	def setValue(device_id, property_name, value)
		executeCommand("update Device set #{property_name} = ? where device_id = ?;", value, device_id)
	end

	# GETTER DATABASE FUNCTIONS
	def getAllDeviceIds()
		rows = getTable("select device_id from Device;")
		return rows.flatten.to_json
	end

	def getAllDevices()
		hashes = getHash("select * from Device;")
		return hashes.to_json
	end

	def getDeviceInfo(device_id)
		hashes = getHash("select * from Device where device_id = ?;", device_id)
		hash = hashes[0] # takes the first element from the resulting array
		return hash.to_json
	end

	# PRIVATE METHODS
	def deviceExists(device_id)
		rows = getTable("select device_id from Device where device_id = ?;", device_id)
		return rows.size > 0 ? true : false
	end

	def insertNewDevice(device_id)
		if deviceExists(device_id)
			return false
		else
			return executeCommand("insert into Device (device_id) values (?);", device_id)
		end
	end

	
	# ALIAS SQL FUNCTIONS
	def executeCommand(query, *bind_vars)
		getTable(query, *bind_vars)
		return true
	end

	def getHash(query, *bind_vars) 
		@db.results_as_hash = true
		result = getTable(query, *bind_vars)
		@db.results_as_hash = false
		return result
	end
	

	# DIRECT SQL FUNCTIONS
	def getTable(query, *bind_vars) #returns
		begin
			result = @db.execute(query, bind_vars)
			return result
		rescue SQLite3::Exception => e
			puts "SQL Error: "
			puts e
		end
	end

	def getScalar(query, *bind_vars)
		begin
			result = @db.get_first_value(query, *bind_vars)	
			return result
		rescue SQLite3::Exception => e
			puts "SQL Error: "
			puts e
		end
	end


end


