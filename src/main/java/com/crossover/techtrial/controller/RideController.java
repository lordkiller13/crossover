/**
 * 
 */
package com.crossover.techtrial.controller;

import java.time.LocalDateTime;
import java.util.*;

import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.crossover.techtrial.dto.TopDriverDTO;
import com.crossover.techtrial.model.Ride;
import com.crossover.techtrial.service.RideService;

/**
 * RideController for Ride related APIs.
 * @author crossover
 *
 */
@RestController
public class RideController {
  
  @Autowired
  RideService rideService;

  @Autowired
  PersonService personService;

  @PostMapping(path ="/api/ride")
  public ResponseEntity<Ride> createNewRide(@RequestBody Ride ride) {
    return ResponseEntity.ok(rideService.save(ride));
  }
  
  @GetMapping(path = "/api/ride/{ride-id}")
  public ResponseEntity<Ride> getRideById(@PathVariable(name="ride-id")Long rideId){
    Ride ride = rideService.findById(rideId);
    if (ride!=null)
      return ResponseEntity.ok(ride);
    return ResponseEntity.notFound().build();
  }
  
  /**
   * This API returns the top 5 drivers with their email,name, total minutes, maximum ride duration in minutes.
   * Only rides that starts and ends within the mentioned durations should be counted.
   * Any rides where either start or endtime is outside the search, should not be considered.
   * 
   * DONT CHANGE METHOD SIGNATURE AND RETURN TYPES
   * @return
   */
  @GetMapping(path = "/api/top-rides")
  public ResponseEntity<List<TopDriverDTO>> getTopDriver(
      @RequestParam(value="max", defaultValue="5") Long count,
      @RequestParam(value="startTime", required = true) @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
      @RequestParam(value="endTime", required = true) @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime){
    List<TopDriverDTO> topDrivers = new ArrayList<TopDriverDTO>();
    /**
     * Your Implementation Here. And Fill up topDrivers Arraylist with Top
     * 
     */
    final List<Ride> ridesList = new ArrayList<>();

    Iterable<Ride> rides = rideService.findAllRides();
    rides.forEach(ridesList::add);

    Map<String, TopDriverDTO> driversDTO = new LinkedHashMap<>();

    for(int i = 0; i < ridesList.size(); ++i) {
      int comparisonStartTime = ridesList.get(i).getStartTime().compareTo(startTime.toString());
      int comparisonEndTime = ridesList.get(i).getEndTime().compareTo(endTime.toString());
      if (comparisonStartTime >= 0 && comparisonEndTime <= 0) {
        Person candidateDriver = personService.findById(ridesList.get(i).getDriver().getId());
        if (driversDTO.get(candidateDriver.getName()) == null) {
          TopDriverDTO candidateDriverDTO = new TopDriverDTO();
          candidateDriverDTO.setName(candidateDriver.getName());
          candidateDriverDTO.setEmail(candidateDriver.getEmail());
          candidateDriverDTO.setTotalRideDurationInSeconds(ridesList.get(i).getDistance());
          candidateDriverDTO.setMaxRideDurationInSecods(ridesList.get(i).getDistance());
          driversDTO.put(candidateDriver.getName(), candidateDriverDTO);
        } else {
          TopDriverDTO candidateDriverDTO = driversDTO.get(candidateDriver.getName());
          candidateDriverDTO.setTotalRideDurationInSeconds(candidateDriverDTO.getTotalRideDurationInSeconds()
                  + ridesList.get(i).getDistance());
          candidateDriverDTO.setMaxRideDurationInSecods(Math.max(ridesList.get(i).getDistance(),
                  candidateDriverDTO.getMaxRideDurationInSecods()));
          driversDTO.put(candidateDriver.getName(), candidateDriverDTO);
        }
      }
    }

    for(Map.Entry<String, TopDriverDTO> driverDTOEntry : driversDTO.entrySet()){
      topDrivers.add(driverDTOEntry.getValue());
    }


    Collections.sort(topDrivers, Comparator.comparing(TopDriverDTO::getMaxRideDurationInSecods));
    
    return ResponseEntity.ok(topDrivers.subList(topDrivers.size() - count.intValue(), topDrivers.size()));
    
  }
  
}
