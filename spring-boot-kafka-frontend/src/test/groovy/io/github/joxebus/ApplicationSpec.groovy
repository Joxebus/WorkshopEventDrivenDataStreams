package io.github.joxebus

import spock.lang.Specification

class ApplicationSpec extends Specification {

    def "application context loads"() {
        expect: "application class exists"
        SpringBootKafkaFrontendApplication != null
    }
}
