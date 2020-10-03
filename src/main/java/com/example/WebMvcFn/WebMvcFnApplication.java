package com.example.WebMvcFn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.servlet.function.RouterFunctions.*;
import static org.springframework.web.servlet.function.ServerResponse.*;

@SpringBootApplication
public class WebMvcFnApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebMvcFnApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes (PetHandler petHandler){
		return route()
				.GET("/pets", petHandler::handleGetAll)
				.GET("/pet/{id}", petHandler::handleGetPetById)
				.POST("/pet", petHandler::handleSavePet)
				.build();
	}
}


@Component
class PetHandler{

	final private PetService petService;

	@Autowired
	public PetHandler(PetService petService){
		this.petService = petService;
	}

	ServerResponse handleGetAll(ServerRequest serverRequest){
		return ok().contentType(APPLICATION_JSON).body(petService.all());
	}

	ServerResponse handleGetPetById(ServerRequest serverRequest){
		return ok().contentType(APPLICATION_JSON).body(petService.getById(Long.parseLong(serverRequest.pathVariable("id"))));
	}

	@SneakyThrows
	ServerResponse handleSavePet(ServerRequest serverRequest){
		Pet result = petService.save(new Pet(null, serverRequest.body(Pet.class).getName()));
		URI uri = URI.create("/pet/" + result.getId());
		return ServerResponse.created(uri).body(result);
	}

}

@Service
class PetService{

	private final AtomicLong counter = new AtomicLong(3L);

	private final List<Pet> pets = List.of(
			new Pet(1L, "Jeff"),
			new Pet(2L, "Luke"),
			new Pet(3L, "Mia")
	);

	List<Pet> all(){
		return pets;
	}

	Pet getById(Long id){
		return pets.stream()
				.filter(pet -> pet.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No " + Pet.class.getName() + " with that ID found!"));
	}

	Pet save(Pet p){
		Pet pet = new Pet(counter.incrementAndGet(), p.getName());
		this.pets.add(pet);
		return pet;
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Pet{
	private Long id;
	private String name;
}