package com.lb_calc_web.controller;

import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.Project;
import com.lb_calc_web.model.utils.*;
import com.lb_calc_web.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ALSService alsService;
    private final LBService lbService;
    private final LCService lcService;
    private final List<Colors> colorsList;
    private final List<PositionLC> positionLCList;
    private final List<TypeLb> typeLbList;
    private final List<DirectionDoorOpening> directionDoorOpeningList;
    private final List<Payment> paymentList;
    private final List<DisplayLC> displayList;
    private final List<BarReader> barReaderList;

    public ProjectController(ProjectService projectService, ALSService aLSService, LBService lbService, LCService lcService) {
        this.projectService = projectService;
        this.alsService = aLSService;
        this.lbService = lbService;
        this.lcService = lcService;
        colorsList = Arrays.asList(Colors.values());
        positionLCList = Arrays.asList(PositionLC.values());
        typeLbList= Arrays.asList(TypeLb.values());
        directionDoorOpeningList = Arrays.asList(DirectionDoorOpening.values());
        paymentList = Arrays.asList(Payment.values());
        displayList = Arrays.asList(DisplayLC.values());
        barReaderList = Arrays.asList(BarReader.values());
    }
    @GetMapping
    private String projects(Model model) {
        model.addAttribute("projects",projectService.findAll());
        return "projects/projects";
    }

    @GetMapping("/{id}")
    private String editProject(@PathVariable(value = "id") Long id, Model model) {
        Optional<Project> projectOptional = projectService.findById(id);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        else {model.addAttribute("error","Project not found");}
        model.addAttribute("project", project);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("positionLCList", positionLCList);
        return "projects/project";
    }
    @GetMapping("/create")
    private String createProject(Model model) {
        Project project = projectService.createProject();
        model.addAttribute("project", project);
        return "redirect:/projects/"+project.getId();
    }

    @PostMapping("/{id}/save")
    public String updateProject(@ModelAttribute("project") Project project) {
        Project updatedProject = projectService.updateProject(project);
        return "redirect:/projects/"+ updatedProject.getId();
    }
    @GetMapping("/{id}/addALS" )
    public String addALS(@PathVariable(value = "id") Long projectId, Model model) {
        model.addAttribute("project",projectService.addALS(projectId));
        return "redirect:/projects/"+projectId;
    }
    @GetMapping("/{id}/alss/{alsId}/delete" )
    public String deleteALS(@PathVariable(value = "id") Long id,
                            @PathVariable(value = "alsId") Long alsId,
                            Model model) {
        model.addAttribute("project", projectService.deleteALS(id, alsId));
        return "redirect:/projects/"+id;
    }
    @GetMapping("/{projectId}/alss/{alsId}" )
    public String editALS(@PathVariable(value = "projectId") Long projectId,
                          @PathVariable(value = "alsId") Long alsId,
                          Model model) {
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("positionLCList", positionLCList);
        Optional<Project> projectOptional =projectService.findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        model.addAttribute("project", project);
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        else {model.addAttribute("error","Als not found");}
        model.addAttribute("als", als);
        byte[] imageBytes= ALSImageService.getBytesArrayALSImage(als);
        String imageString= Base64.getEncoder().encodeToString(imageBytes);
        model.addAttribute("image", imageString);
        return "projects/project_als";
    }
    @PostMapping("/{projectId}/alss/{alsId}/save")
    public String saveALS(
            @PathVariable(value = "projectId") Long projectId,
            @PathVariable(value = "alsId") Long alsId,
            @ModelAttribute("als") ALS als,
            Model model) {
        Optional<Project> projectOptional =projectService.findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        model.addAttribute("project", project);

        als=projectService.updateALS(project,als, alsId);
        return "redirect:/projects/"+projectId+"/alss/"+als.getId();
    }
    @GetMapping("/{projectId}/alss/{alsId}/addLB" )
    public String addLB(@PathVariable(value = "projectId") Long projectId,
                            @PathVariable(value = "alsId") Long alsId,
                            Model model) {
        ALS als=projectService.addLB(projectId,alsId);
        model.addAttribute("als", als);
        return "redirect:/projects/"+projectId+"/alss/"+als.getId();
    }
    @GetMapping("/{projectId}/alss/{alsId}/lbs/{lbId}/delete" )
    public String deleteLB(@PathVariable(value = "projectId") Long projectId,
                            @PathVariable(value = "alsId") Long alsId,
                           @PathVariable(value = "lbId") Long lbId,
                        Model model) {
        ALS als=projectService.deleteLB(projectId, alsId, lbId);
        model.addAttribute("als", als);
        return "redirect:/projects/"+projectId+"/alss/"+als.getId();
    }

    @GetMapping("/{projectId}/alss/{alsId}/lbs/{lbId}")
    private String editLB(@PathVariable(value = "projectId") Long projectId,
                          @PathVariable(value = "alsId") Long alsId,
                          @PathVariable(value = "lbId") Long lbId,
                          Model model) {
        Optional<Project> projectOptional = projectService.findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        Optional<LB> lbOptional = lbService.findById(lbId);
        LB lb = null;
        if (lbOptional.isPresent()) {lb = lbOptional.get();}
        else {model.addAttribute("error","LB not found");}
        model.addAttribute("project", project);
        model.addAttribute("als", als);
        model.addAttribute("lb", lb);
        model.addAttribute("typeLbList", typeLbList);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);
        byte[] imageBytes= LBImageService.getBytesArrayLBImage(lb);
        String imageString= Base64.getEncoder().encodeToString(imageBytes);
        model.addAttribute("image", imageString);
        return "projects/project_lb";
    }
    @PostMapping("/{projectId}/alss/{alsId}/lbs/{lbId}/save")
    public String saveLB(@PathVariable(value = "projectId") Long projectId,
                        @PathVariable(value = "alsId") Long alsId,
                         @PathVariable(value = "lbId") Long lbId,
                         @ModelAttribute("lb") LB lb,
                         Model model) {
        Optional<Project> projectOptional = projectService.findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        model.addAttribute("project", project);
        model.addAttribute("als", als);
        Optional<LB> lbOptional = lbService.findById(lbId);
        LB lbOld = null;
        if (lbOptional.isPresent()) {lbOld = lbOptional.get();}

        als=projectService.updateLB(project,als,lb,lbOld);
        for(LB lb1:als.getLbList()){
            if(lb1.equals(lb)){
                lbId= (long) lb1.getId();
                break;
            }
        }
        return "redirect:/projects/"+projectId+"/alss/"+als.getId()+"/lbs/"+lbId;
    }
    @GetMapping("/{projectId}/alss/{alsId}/lcs/{lcId}")
    private String editLC(@PathVariable(value = "projectId") Long projectId,
                          @PathVariable(value = "alsId") Long alsId,
                          @PathVariable(value = "lcId") Long lcId,
                          Model model) {
        Optional<Project> projectOptional = projectService.findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        Optional<LC> lcOptional = lcService.findById(lcId);
        LC lc = null;
        if (lcOptional.isPresent()) {lc = lcOptional.get();}
        else {model.addAttribute("error","LC not found");}
        model.addAttribute("project", project);
        model.addAttribute("als", als);
        model.addAttribute("lc", lc);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        byte[] imageBytes= LCImageService.getBytesArrayLCImage(lc);
        String imageString= Base64.getEncoder().encodeToString(imageBytes);
        model.addAttribute("image", imageString);
        return "projects/project_lc";
    }
    @PostMapping("/{projectId}/alss/{alsId}/lcs/{lcId}/save")
    public String updateLC(
            @PathVariable(value = "projectId") Long projectId,
            @PathVariable(value = "alsId") Long alsId,
            @PathVariable(value = "lcId") Long lcId,
            @ModelAttribute("lc") LC lc,
            Model model) {
        Optional<Project> projectOptional = projectService.findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        model.addAttribute("project", project);
        model.addAttribute("als", als);

        als=projectService.updateLC(project,als,alsId,lc);
        return "redirect:/projects/"+projectId+"/alss/"+ als.getId()+"/lcs/"+als.getLc().getId();
    }
}
