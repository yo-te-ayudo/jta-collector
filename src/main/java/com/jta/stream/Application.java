package com.jta.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
public class Application {

    public static void main(String[] args) {
        List<PersonDTO> personDTOList = new PersonRepo().getAll();

        Person person = personDTOList.stream().collect(new PersonCollector());
        Application.printPerson(person, "");
    }

    private static void printPerson(Person person, String tab){
        System.out.println(tab + person.name);
        if(person.teamMembers!=null) {
            person.teamMembers.forEach(p -> Application.printPerson(p, tab + "\t"));
        }
    }
}

class PersonCollector implements Collector<PersonDTO, Map<Integer, List<Person>>, Person> {

    @Override
    public Supplier<Map<Integer, List<Person>>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<Integer, List<Person>>, PersonDTO> accumulator() {
        return (accumulator, personDto) -> {
            accumulator.putIfAbsent(personDto.parentId, new ArrayList<>());
            accumulator.computeIfPresent(personDto.parentId, (key, personList) -> {
                Person person = new Person();
                person.id = personDto.id;
                person.name = personDto.name;
                personList.add(person);
                return personList;
            });
        };
    }

    @Override
    public BinaryOperator<Map<Integer, List<Person>>> combiner() {
        return null;
    }

    @Override
    public Function<Map<Integer, List<Person>>, Person> finisher() {
        return accumulator -> {
            accumulator.values().stream().flatMap(Collection::stream)
                    .forEach(person -> person.teamMembers = accumulator.get(person.id));
            return accumulator.get(null).get(0);
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}

class Person{
    Integer id;
    String name;
    List<Person> teamMembers;
}

class PersonRepo{

    List<PersonDTO> getAll(){
        return Arrays.asList(
                new PersonDTO(1, "Maria", null),

                new PersonDTO(2, "Juan", 1),
                new PersonDTO(3, "Luisa", 1),

                new PersonDTO(4, "Dolores", 2),
                new PersonDTO(5, "Pepe", 2),
                new PersonDTO(6, "Elena", 2),

                new PersonDTO(7, "Antonio", 3),
                new PersonDTO(8, "Pedro", 3),

                new PersonDTO(9, "Monica", 8)
        );
    }
}

class PersonDTO {

    public PersonDTO(Integer id, String name, Integer parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    Integer id;
    String name;
    Integer parentId;
}
