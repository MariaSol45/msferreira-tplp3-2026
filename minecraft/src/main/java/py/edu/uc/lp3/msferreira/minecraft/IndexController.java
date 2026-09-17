package py.edu.uc.lp3.msferreira.minecraft;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

	@GetMapping("/")
	public String index() {
		return "Hola Mundo";
	}
}
