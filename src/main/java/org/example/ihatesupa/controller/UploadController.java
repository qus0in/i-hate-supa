package org.example.ihatesupa.controller;

import lombok.extern.java.Log;
import org.example.ihatesupa.model.form.MyForm;
import org.example.ihatesupa.service.StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Log
public class UploadController {
    private final StorageService storageService;

    public UploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public String upload(Model model) {
        model.addAttribute("form", new MyForm(null));
        return "index";
    }

    @PostMapping
    public String uploadSubmit(MyForm form, RedirectAttributes redirectAttributes) throws Exception {
//        log.info(form.toString());
        String filenameOnServer = storageService.uploadFile(form.file());
        log.info(filenameOnServer);
//        return "redirect:/";
        redirectAttributes.addFlashAttribute("filename", filenameOnServer);
        return "redirect:/";
    }

    @GetMapping("/img/{fileName}")
    public ResponseEntity<byte[]> download(@PathVariable String fileName) {
        try {
            byte[] fileBytes = storageService.downloadFile(fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
