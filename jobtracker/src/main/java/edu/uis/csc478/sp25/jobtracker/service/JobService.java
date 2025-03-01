package edu.uis.csc478.sp25.jobtracker.service;

import edu.uis.csc478.sp25.jobtracker.model.Job;
import edu.uis.csc478.sp25.jobtracker.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.http.HttpStatus.*;

@Service
public class JobService {
     private final JobRepository repository;

     public JobService(JobRepository repository) {
          this.repository = repository;
     }

     public List<Job> getAllJobsForUser(UUID userId) {
          return repository.findAllByUser_id(userId);
     }

     /**
      * @param newJob a Job entity
      * @param userId the ID of the user creating the job
      * @return a ResponseEntity with a status of either CONFLICT or CREATED
      */
     public ResponseEntity<String> createJob(Job newJob, UUID userId) {
          if (newJob.id == null) {
               newJob.id = UUID.randomUUID();
          } else if (repository.existsById(newJob.id)) {
               return new ResponseEntity<>("Job already exists.", CONFLICT);
          }
          newJob.user_id = userId;
          repository.save(newJob);
          return new ResponseEntity<>("Job created successfully.", CREATED);
     }

     // Check if a job exists by ID
     public boolean existsByUUID(UUID jobID) {
          return repository.existsById(jobID);
     }

     /**
      * @param jobID  a particular id of a job
      * @param userId the ID of the user requesting the job
      * @return a ResponseEntity with a status of either OK or NOT FOUND
      */
     public ResponseEntity<Job> getJobById(UUID jobID, UUID userId) {
          Optional<Job> job = repository.findByIdAndUser_id(jobID, userId);

          if (job.isPresent()) {
               return new ResponseEntity<>(job.get(), OK);
          } else {
               return new ResponseEntity<>(NOT_FOUND);
          }
     }

     /**
      * @param jobID      the id of the job to be updated
      * @param updatedJob the new job details to take the place of the current values
      * @param userId     the ID of the user updating the job
      * @return a ResponseEntity that either signals OK or throws an error
      */
     public ResponseEntity<String> updateJobById(UUID jobID, Job updatedJob, UUID userId) {
          Optional<Job> existingJob = repository.findByIdAndUser_id(jobID, userId);

          if (existingJob.isPresent()) {
               return applyJobUpdates(existingJob.get(), updatedJob);
          } else {
               return new ResponseEntity<>("Job not found or you don't have permission to update it.", NOT_FOUND);
          }
     }

     /**
      * @param existingJob an existing job
      * @param updatedJob  an updated job
      * @return a ResponseEntity with a status indicating whether the update happened or not
      */
     private ResponseEntity<String> applyJobUpdates(Job existingJob, Job updatedJob) {
          // Exclude 'id' and 'user_id' to prevent overwriting
          copyProperties(updatedJob, existingJob, "id", "user_id");
          repository.save(existingJob);
          return new ResponseEntity<>("Job updated successfully.", OK);
     }

     /**
      * @param userId    the ID of the user performing the search
      * @param title     an optional parameter for job titles
      * @param level     an optional parameter for values like "entry level" or "senior" or "director" or others
      * @param minSalary an optional parameter for the minimum matching salary
      * @param maxSalary an optional parameter for the maximum matching salary
      * @param location  an optional parameter for the location in which the job is advertised
      * @return the jobs that match the filters applied by the parameters that were given, including the user's jobs and potentially other visible jobs
      */
     public List<Job> searchJobs(UUID userId,
                                 String title,
                                 String level,
                                 Integer minSalary,
                                 Integer maxSalary,
                                 String location) {
          return repository.findByFilters(userId, title, level, minSalary, maxSalary, location);
     }

     /**
      * @param jobId  of the job to be deleted
      * @param userId the ID of the user deleting the job
      * @return a ResponseEntity with a status of the deletion
      */
     public ResponseEntity<String> deleteJob(UUID jobId, UUID userId) {
          if (!repository.existsByIdAndUser_id(jobId, userId)) {
               return new ResponseEntity<>("Job not found or you don't have permission to delete it.", NOT_FOUND);
          }

          repository.deleteById(jobId);
          return new ResponseEntity<>("Job deleted successfully.", OK);
     }
}