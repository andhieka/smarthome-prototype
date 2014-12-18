require 'socket'

class IntegClient
	def initialize(hostname, port)
		@hostname = hostname
		@port = port

		print("List of devices hosted on server #{hostname}:#{port}:\n")
		dev = sendMessage("LISTNAME: ")
		print dev
	end

	def sendMessage(msg)
		socket = TCPSocket.open(@hostname, @port)
		socket.puts(msg)
		socket.puts("\n$$$END_OF_MESSAGE$$$")
		response = ""
		while line = socket.gets
			response += line
		end
		socket.close
		return response
	end
end


loop {
	unless @ic
		print "Please enter hostname: "
		hostname = gets.chomp
		print "Please enter port: "
		port = gets.chomp
		@ic = IntegClient.new(hostname, port)
	end
	if @ic
		print "IntegClient >>> "
		input = gets.chomp
		response = @ic.sendMessage(input)
		print response
	else
		print "ERROR cannot be resolved."
		return
	end


}

