package com.example.temporal.web;

import com.example.temporal.config.TemporalConfig;
import com.example.temporal.workflow.DemoWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    private final WorkflowClient client;

    public WorkflowController(WorkflowClient client) {
        this.client = client;
    }

    // POST /workflow/start?input=hello  → starts the workflow
    @PostMapping("/start")
    public Map<String, String> start(
            @RequestParam(defaultValue = "hello") String input) {

        String workflowId = "demo-" + UUID.randomUUID();

        DemoWorkflow workflow = client.newWorkflowStub(
                DemoWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(TemporalConfig.TASK_QUEUE)
                        .build()
        );

        WorkflowClient.start(workflow::run, input);

        return Map.of(
                "workflowId", workflowId,
                "status", "started",
                "note", "Workflow is running activities A & B, then waiting for a signal"
        );
    }

    // GET /workflow/{id}/status  → query current status
    @GetMapping("/{workflowId}/status")
    public ResponseEntity<Map<String, String>> status(
            @PathVariable String workflowId) {
        try {
            DemoWorkflow workflow = client.newWorkflowStub(DemoWorkflow.class, workflowId);
            return ResponseEntity.ok(Map.of(
                    "workflowId", workflowId,
                    "status", workflow.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /workflow/{id}/signal?message=go  → send the proceed signal
    @PostMapping("/{workflowId}/signal")
    public ResponseEntity<Map<String, String>> signal(
            @PathVariable String workflowId,
            @RequestParam(defaultValue = "go!") String message) {
        try {
            DemoWorkflow workflow = client.newWorkflowStub(DemoWorkflow.class, workflowId);
            workflow.proceed(message);
            return ResponseEntity.ok(Map.of(
                    "workflowId", workflowId,
                    "signaled", "true",
                    "message", message
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
