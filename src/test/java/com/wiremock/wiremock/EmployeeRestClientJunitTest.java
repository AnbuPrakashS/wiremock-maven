package com.wiremock.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.util.ResourceUtils;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class EmployeeRestClientJunitTest {

	private static int idCounter = 1;
	private List<Employee> ALL_EMPLOYEES = new ArrayList<>();


	@Rule
	public WireMockRule wireMockRule = new WireMockRule(options().port(8888).httpsPort(9999));

	private static Employee buildEmployee(String firstName, String lastName) {
		Employee emp = new Employee();
		emp.setId(idCounter);
		emp.setFirstName(firstName);
		emp.setLastName(lastName);

		idCounter++;

		return emp;
	}

	@Before
	public void initializeEmployees() {
		Employee emp1 = buildEmployee("Deepak", "Moud");
		Employee emp2 = buildEmployee("Srinivasa Rao", "Gumma");
		Employee emp3 = buildEmployee("Purna Chandra", "Rao");
		Employee emp4 = buildEmployee("Madhavi Latha", "Gumma");
		Employee emp5 = buildEmployee("Raghava", "Reddy");

		ALL_EMPLOYEES.addAll(Arrays.asList(emp1, emp2, emp3, emp4, emp5));

	}

	@Test
	public void allEmployeeForEactURLPath() throws ParseException, IOException {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/employees")).willReturn(aResponse().withStatus(200)
				.withHeader("Content-Type", "application/json").withBody(Json.write(ALL_EMPLOYEES))));

		HttpGet request = new HttpGet("http://localhost:8888/api/v1/employees");
		String response = EntityUtils.toString(HttpClients.createDefault().execute(request).getEntity());
		
		System.out.println("-----------------------------------");
		System.out.println(response);
		System.out.println("-----------------------------------");
		Assert.assertNotNull(response);
		Assert.assertTrue(JsonParserFactory.getJsonParser().parseList(response).size()==5);
		verify(exactly(1), getRequestedFor(urlPathEqualTo("/api/v1/employees")));
	}
	
	@Test
	public void allEmployeeFromJsonFile() throws ParseException, IOException {
		File file = ResourceUtils.getFile("classpath:data/employee.json");
		stubFor(get(urlPathEqualTo("/api/v1/employees")).willReturn(aResponse().withStatus(200)
				.withHeader("Content-Type", "application/json").withBody(new String(Files.readAllBytes(file.toPath())))));

		HttpGet request = new HttpGet("http://localhost:8888/api/v1/employees");
		String response = EntityUtils.toString(HttpClients.createDefault().execute(request).getEntity());
		
		System.out.println("==========================================");
		System.out.println(response);
		System.out.println("============================================");
		
		Assert.assertNotNull(response);
		Assert.assertTrue(JsonParserFactory.getJsonParser().parseList(response).size()==6);
		verify(exactly(1), getRequestedFor(urlPathEqualTo("/api/v1/employees")));
	}

}
