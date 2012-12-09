package br.com.fixturefactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import br.com.fixturefactory.model.Address;
import br.com.fixturefactory.model.City;
import br.com.fixturefactory.model.Immutable;
import br.com.fixturefactory.model.Immutable.ImmutableInner;
import br.com.fixturefactory.model.Route;
import br.com.fixturefactory.model.RouteId;

public class FixtureImmutableTest {

	@Before
	public void setUp() {
		Fixture.of(Immutable.class)
			.addTemplate("twoParameterConstructor", new Rule() {{
				add("propertyA", regex("\\w{8}"));
				add("propertyB", random(1000L, 2000L));
				add("immutableInner", fixture(ImmutableInner.class, "immutable"));
			}})
			.addTemplate("threeParameterConstructor", new Rule() {{
				add("propertyB", random(1000L, 2000L));
				add("propertyC", regex("\\w{8}"));
				add("immutableInner", fixture(ImmutableInner.class, "immutable"));
				add("address", fixture(Address.class, "valid"));
			}})
			.addTemplate("fullConstructor", new Rule() {{
				add("propertyA", regex("\\w{8}"));
				add("propertyB", random(1000L, 2000L));
				add("propertyC", "${propertyA} based");
				add("date", instant("now"));
				add("address", fixture(Address.class, "valid"));
			}});
		Fixture.of(ImmutableInner.class)
			.addTemplate("immutable", new Rule() {{ 
				add("propertyD", regex("\\w{8}"));
			}});
		
		Fixture.of(Address.class).addTemplate("valid", new Rule(){{
			add("id", random(Long.class, range(1L, 100L)));
			add("street", random("Paulista Avenue", "Ibirapuera Avenue"));
			add("city", "São Paulo");
			add("state", "${city}");
			add("country", "Brazil");
			add("zipCode", random("06608000", "17720000"));
		}});
		
        Fixture.of(Route.class).addTemplate("valid", new Rule() {{
    		add("id", one(RouteId.class, "valid"));
            add("cities", has(2).of(City.class, "valid"));
        }});
        
        Fixture.of(RouteId.class).addTemplate("valid", new Rule() {{
        	add("value", 1L);
        }});
        
        Fixture.of(City.class).addTemplate("valid", new Rule() {{
            add("name", regex("\\w{8}"));
        }});
	}
	
	@Test
	public void shouldCreateImmutableObjectUsingPartialConstructor() {
		Immutable result = Fixture.from(Immutable.class).gimme("twoParameterConstructor");
		
		assertNotNull(result.getPropertyA());
		assertNotNull(result.getPropertyB());
		assertEquals("default", result.getPropertyC());
		assertNotNull(result.getImmutableInner().getPropertyD());
		assertNull(result.getDate());
		assertNull(result.getAddress());
	}

	@Test
	public void shouldCreateImmutableObjectUsingAnotherPartialConstructor() {
		Immutable result = Fixture.from(Immutable.class).gimme("threeParameterConstructor");
		
		assertEquals("default", result.getPropertyA());
		assertNotNull(result.getPropertyB());
		assertNotNull(result.getPropertyC());
		assertNotNull(result.getImmutableInner().getPropertyD());
		assertNull(result.getDate());
		assertNotNull(result.getAddress());
	}
	
	@Test
	public void shouldCreateImmutableObjectUsingFullConstructor() {
		Immutable result = Fixture.from(Immutable.class).gimme("fullConstructor");
		
		assertNotNull(result.getPropertyA());
		assertNotNull(result.getPropertyB());
		assertEquals(result.getPropertyA() + " based", result.getPropertyC());
		assertNotNull(result.getDate());
		assertNotNull(result.getAddress());
		assertEquals(result.getAddress().getCity(), result.getAddress().getState());
	}
	
	@Test
	public void shouldWorkWhenReceivingRelationsInTheConstructor() {
		Route route = Fixture.from(Route.class).gimme("valid");
		assertEquals(Long.valueOf(1L), route.getId().getValue());
		assertNotNull(route.getCities().get(0).getName());
	}
}
