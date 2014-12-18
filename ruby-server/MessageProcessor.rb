
class MessageProcessor
	def initialize(dd)
		@dd = dd
	end

	def process(msg)
		msg.strip!
		if /GET:/ =~ msg
			cmd, dev, junk = msg.split(/[:;]/)
			return @dd.getDeviceInfo(dev.strip)
		elsif /SET:/ =~ msg
			cmd, dev, prop, val, junk = msg.split(/[:;]/)
			return @dd.setValue(dev.strip, prop.strip, val.strip)
		elsif /LISTDEVICES:/ =~ msg
			return @dd.getAllDevices()
		elsif /LISTNAME:/ =~ msg
			return @dd.getAllDeviceIds()
		else
			return "Invalid message."
		end
	end

end
