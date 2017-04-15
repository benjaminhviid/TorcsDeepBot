package itu.bj.torcs;

public class RunTorcs {


	public static void main(String[] args) {

		String[] arguments = {
				"itu.bj.torcs.TorcsNeatController",
				"host:localhost",
				"port:3001",
				"maxEpisodes:1",
				"maxSteps:10000",
				"trackName:aalborg",
				"stage:2"
		};
		Client.setDriver(new DrunkKitt());
        Client.main(arguments);
	}


	}
