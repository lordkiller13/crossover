/**
 * 
 */
package com.crossover.techtrial.controller;

import com.crossover.techtrial.service.PersonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.repositories.PersonRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author kshah
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PersonControllerTest {

  MockMvc mockMvc;
  
  @Mock
  private PersonController personController;
  
  @Autowired
  private TestRestTemplate template;
  
  @Autowired
  PersonRepository personRepository;

  @Autowired
  PersonService personService;
  
  @Before
  public void setup() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(personController).build();
  }
  
  @Test
  public void testPanelShouldBeRegistered() throws Exception {
    HttpEntity<Object> person = getHttpEntity(
        "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\"," 
            + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");
    ResponseEntity<Person> response = template.postForEntity(
        "/api/person", person, Person.class);
    //Delete this user
    personRepository.deleteById(response.getBody().getId());
    assertEquals("test 1", response.getBody().getName());
    assertEquals(200,response.getStatusCode().value());
  }

  private HttpEntity<Object> getHttpEntity(Object body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<Object>(body, headers);
  }

  @Test
  public void register_HappyCase_Test() throws Exception {

    Person mockPerson = mock(Person.class);
    when(personService.save(mockPerson)).thenReturn(mockPerson);

    mockMvc.perform(MockMvcRequestBuilders.post("/api/person").accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk());


    Person actualPerson = personService.save(mockPerson);

    assertEquals(mockPerson, actualPerson);
    verify(personService, times(2)).save(any(Person.class));
  }

  @Test
  public void getAllPersons_HappyCase_Test() throws Exception {
    List<Person> mockPersonList = new ArrayList<>();
    mockPersonList.add(mock(Person.class));

    when(personService.getAll()).thenReturn(mockPersonList);

    mockMvc.perform(MockMvcRequestBuilders.get("/api/person").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    List<Person> actualPersonList = personService.getAll();

    assertEquals(mockPersonList, actualPersonList);

    verify(personService, times(2)).getAll();
  }

  @Test
  public void getPersonById_HappyCase_Test() throws Exception {
    Long dummyPersonId = 0L;

    Person mockPerson = mock(Person.class);
    when(personService.findById(dummyPersonId)).thenReturn(mockPerson);


    mockMvc.perform(MockMvcRequestBuilders.get("/api/person/{person-id}").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    Person actualPerson = personService.findById(dummyPersonId);

    assertEquals(mockPerson, actualPerson);
    verify(personService, times(2)).findById(anyLong());
  }

  @Test
  public void getPersonById_EntityNotFound_ReturnNotFound_Test() throws Exception {
    Long dummyPersonId = 0L;

    Person mockPerson = mock(Person.class);
    when(personService.findById(dummyPersonId)).thenReturn(null);

    mockMvc.perform(MockMvcRequestBuilders.get("/api/person/{person-id}").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

    verify(personService, times(1)).findById(anyLong());
  }
}
