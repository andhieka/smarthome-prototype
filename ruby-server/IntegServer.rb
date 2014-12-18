require 'socket'
require 'logger'
require_relative 'DeviceDatabase'
require_relative 'MessageProcessor'

port = 1928
logger = Logger.new(STDOUT)
logger.level = Logger::DEBUG
logger.info("IntegHome Server is up and running at port #{port}.")

dd = DeviceDatabase.new()
mp = MessageProcessor.new(dd)

counter = 0

server = TCPServer.open(port)
loop {
	Thread.start(server.accept) do |client| 
		counter += 1
		@counter = counter
		logger.debug("Client #{@counter} Connected")
		begin
			input = ""
			while line = client.gets
				input += line
				break if /\$\$\$END_OF_MESSAGE\$\$\$/ =~ line
			end
			logger.debug("Message: #{input}")
			response = mp.process(input)
			logger.debug("Response: #{response}")
			client.puts(response)
		rescue StandardError => e
			puts "IntegServer -- ERROR: "
			puts e
			logger.error(e)
		ensure
			client.close if client
		end
		logger.debug("Client #{@counter} Disconnected...")
	end
}

