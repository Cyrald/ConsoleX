module Console{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.reflections;
    requires java.base;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
	requires java.desktop;
    
    
    opens Core to javafx.fxml;
    opens ui to javafx.fxml;
    exports Core;
    exports ui;
    exports command;
    exports command.impl;
}
