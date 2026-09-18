-- ============================================================
-- Employee
-- ============================================================

CREATE TABLE employee (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          first_name VARCHAR(255) NOT NULL,
                          last_name VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL,
                          encrypted_password VARCHAR(255) NOT NULL,
                          registration_date DATE NOT NULL,
                          role VARCHAR(50) NOT NULL,

                          CONSTRAINT pk_employee
                              PRIMARY KEY (id),

                          CONSTRAINT uk_employee_email
                              UNIQUE (email)
) ENGINE=InnoDB;


-- ============================================================
-- Base module
-- JOINED inheritance root
-- ============================================================

CREATE TABLE module (
                        id BIGINT NOT NULL AUTO_INCREMENT,

                        name VARCHAR(255) NOT NULL,
                        description TEXT,

                        height INT NOT NULL,
                        width INT NOT NULL,
                        depth INT NOT NULL,

                        upper_frame INT NOT NULL,
                        bottom_frame INT NOT NULL,

                        color_body VARCHAR(50) NOT NULL,
                        color_door VARCHAR(50) NOT NULL,

                        CONSTRAINT pk_module
                            PRIMARY KEY (id)
) ENGINE=InnoDB;


-- ============================================================
-- LC
-- ============================================================

CREATE TABLE lc (
                    id BIGINT NOT NULL,

                    display VARCHAR(255),
                    bar_reader VARCHAR(255),
                    payment VARCHAR(50),

                    printer BOOLEAN NOT NULL,
                    rfid_reader BOOLEAN NOT NULL,

                    CONSTRAINT pk_lc
                        PRIMARY KEY (id),

                    CONSTRAINT fk_lc_module
                        FOREIGN KEY (id)
                            REFERENCES module (id)
                            ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- LB
-- ============================================================

CREATE TABLE lb (
                    id BIGINT NOT NULL,

                    type VARCHAR(255) NOT NULL,
                    delta_width INT NOT NULL,
                    shelf_thick INT NOT NULL,
                    service_zone_width INT NOT NULL,

                    door_thickness INT NOT NULL,
                    direction_door_opening VARCHAR(50) NOT NULL,

                    count_cells INT NOT NULL,
                    height_cell INT NOT NULL,
                    width_cell INT NOT NULL,
                    depth_cell INT NOT NULL,

                    CONSTRAINT pk_lb
                        PRIMARY KEY (id),

                    CONSTRAINT fk_lb_module
                        FOREIGN KEY (id)
                            REFERENCES module (id)
                            ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- LBC
-- ============================================================

CREATE TABLE lbc (
                     id BIGINT NOT NULL,

                     type VARCHAR(255) NOT NULL,
                     delta_width INT NOT NULL,
                     shelf_thick INT NOT NULL,
                     service_zone_width INT NOT NULL,

                     door_thickness INT NOT NULL,
                     direction_door_opening VARCHAR(50) NOT NULL,

                     count_cells INT NOT NULL,
                     height_cell INT NOT NULL,
                     width_cell INT NOT NULL,
                     depth_cell INT NOT NULL,

                     display VARCHAR(255),
                     bar_reader VARCHAR(255),
                     payment VARCHAR(50),

                     printer BOOLEAN NOT NULL,
                     rfid_reader BOOLEAN NOT NULL,

                     CONSTRAINT pk_lbc
                         PRIMARY KEY (id),

                     CONSTRAINT fk_lbc_module
                         FOREIGN KEY (id)
                             REFERENCES module (id)
                             ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- LC access methods
-- ============================================================

CREATE TABLE lc_access_method (
                                  lc_id BIGINT NOT NULL,
                                  access_method VARCHAR(50) NOT NULL,

                                  CONSTRAINT pk_lc_access_method
                                      PRIMARY KEY (lc_id, access_method),

                                  CONSTRAINT fk_lc_access_method_lc
                                      FOREIGN KEY (lc_id)
                                          REFERENCES lc (id)
                                          ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- LC print options
-- ============================================================

CREATE TABLE lc_print_option (
                                 lc_id BIGINT NOT NULL,
                                 print_option VARCHAR(50) NOT NULL,

                                 CONSTRAINT pk_lc_print_option
                                     PRIMARY KEY (lc_id, print_option),

                                 CONSTRAINT fk_lc_print_option_lc
                                     FOREIGN KEY (lc_id)
                                         REFERENCES lc (id)
                                         ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- LBC access methods
-- ============================================================

CREATE TABLE lbc_access_method (
                                   lbc_id BIGINT NOT NULL,
                                   access_method VARCHAR(50) NOT NULL,

                                   CONSTRAINT pk_lbc_access_method
                                       PRIMARY KEY (lbc_id, access_method),

                                   CONSTRAINT fk_lbc_access_method_lbc
                                       FOREIGN KEY (lbc_id)
                                           REFERENCES lbc (id)
                                           ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- LBC print options
-- ============================================================

CREATE TABLE lbc_print_option (
                                  lbc_id BIGINT NOT NULL,
                                  print_option VARCHAR(50) NOT NULL,

                                  CONSTRAINT pk_lbc_print_option
                                      PRIMARY KEY (lbc_id, print_option),

                                  CONSTRAINT fk_lbc_print_option_lbc
                                      FOREIGN KEY (lbc_id)
                                          REFERENCES lbc (id)
                                          ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- ALS
-- ============================================================

CREATE TABLE als (
                     id BIGINT NOT NULL AUTO_INCREMENT,

                     name VARCHAR(255) NOT NULL,
                     description TEXT,

                     height INT NOT NULL,
                     width INT NOT NULL,
                     depth INT NOT NULL,

                     upper_frame INT NOT NULL,
                     bottom_frame INT NOT NULL,

                     color_body VARCHAR(50) NOT NULL,
                     color_door VARCHAR(50) NOT NULL,

                     CONSTRAINT pk_als
                         PRIMARY KEY (id)
) ENGINE=InnoDB;


-- ============================================================
-- ALS modules
-- Physical order of modules inside ALS
-- ============================================================

CREATE TABLE als_module (
                            id BIGINT NOT NULL AUTO_INCREMENT,

                            als_id BIGINT NOT NULL,
                            module_id BIGINT NOT NULL,
                            module_order INT NOT NULL,

                            CONSTRAINT pk_als_module
                                PRIMARY KEY (id),

                            CONSTRAINT uk_als_module_order
                                UNIQUE (als_id, module_order),

                            CONSTRAINT fk_als_module_als
                                FOREIGN KEY (als_id)
                                    REFERENCES als (id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_als_module_module
                                FOREIGN KEY (module_id)
                                    REFERENCES module (id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT chk_als_module_order
                                CHECK (module_order >= 0)
) ENGINE=InnoDB;


-- ============================================================
-- Project
-- ============================================================

CREATE TABLE project (
                         id BIGINT NOT NULL AUTO_INCREMENT,

                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         company VARCHAR(255) NOT NULL,

                         created_at DATE NOT NULL,
                         created_by_id BIGINT NOT NULL,

                         updated_at DATE NOT NULL,
                         updated_by_id BIGINT NOT NULL,

                         CONSTRAINT pk_project
                             PRIMARY KEY (id),

                         CONSTRAINT fk_project_created_by
                             FOREIGN KEY (created_by_id)
                                 REFERENCES employee (id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT fk_project_updated_by
                             FOREIGN KEY (updated_by_id)
                                 REFERENCES employee (id)
                                 ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- Project ALS
-- ============================================================

CREATE TABLE project_als (
                             id BIGINT NOT NULL AUTO_INCREMENT,

                             project_id BIGINT NOT NULL,
                             als_id BIGINT NOT NULL,
                             quantity INT NOT NULL,

                             CONSTRAINT pk_project_als
                                 PRIMARY KEY (id),

                             CONSTRAINT uk_project_als
                                 UNIQUE (project_id, als_id),

                             CONSTRAINT fk_project_als_project
                                 FOREIGN KEY (project_id)
                                     REFERENCES project (id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_project_als_als
                                 FOREIGN KEY (als_id)
                                     REFERENCES als (id)
                                     ON DELETE RESTRICT,

                             CONSTRAINT chk_project_als_quantity
                                 CHECK (quantity >= 1)
) ENGINE=InnoDB;